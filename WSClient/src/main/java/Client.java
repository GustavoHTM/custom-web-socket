import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

// todo: Inverter dependencia, fazer chat instanciar o client ou receber uma instancia de client
public class Client {

    private static final int SERVER_PORT_NUMBER = 4000;
    private static final String IP_LOCALHOST = "127.0.0.1";

    private static final String END_OF_MESSAGE = "<END>";
    private static final String ERROR_MESSAGE = "<ERROR>";

    public static Socket server;
    private static PrintStream output;

    public static void main(String[] args) {
        try {
            serverConnect();
            output = new PrintStream(server.getOutputStream(), true);

            new NameSelectorFrame(name -> {
                ClientUI clientUI = new SimpleChatPanel(output);

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        processReceiveServerMessage(clientUI);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }, output);

        } catch (Exception exception) {
            System.out.println("Erro: " + exception.getMessage() + ", Abortando...");
        }
    }

    private static void processReceiveServerMessage(ClientUI clientUI) throws IOException {

        try (Scanner input = new Scanner(server.getInputStream())) {
            while (input.hasNextLine()) {
                StringBuilder message = new StringBuilder();
                String from = "Server";
                boolean isError;

                String firstLine = input.nextLine();
                if (!(isError = firstLine.equals(ERROR_MESSAGE))) {
                    from = firstLine;
                }

                String line = input.nextLine();
                while (!line.equals(END_OF_MESSAGE)) {
                    message.append(line).append("\n");
                    line = input.nextLine();
                }

                clientUI.receiveMessage(from, message.toString(), isError);
            }
        } catch (Exception exception) {
            System.out.println("Houve um problema de conexão com o servidor, Erro: " + exception.getMessage());
        } finally {
            server.close();
        }
    }

    private static void serverConnect() throws IOException {
        try {
            server = new Socket(IP_LOCALHOST, SERVER_PORT_NUMBER);
            output = new PrintStream(server.getOutputStream());
            System.out.println("Conectado com o servidor com sucesso");
        } catch (Exception exception) {
            System.out.println("Erro ao conectar com servidor, erro: " + exception.getMessage());
            throw exception;
        }
    }

    public static void closeConnection() {
        if (server != null && !server.isClosed()) {
            try {
                output.println("/exit\n<END>");
                output.close();
                server.close();
            } catch (IOException exception) {
                System.out.println("Erro ao fechar a conexão, erro: " + exception.getMessage());
            }
        }

        System.exit(0);
    }
}
