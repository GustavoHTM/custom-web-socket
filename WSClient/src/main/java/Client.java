import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Client {

    private static final int SERVER_PORT_NUMBER = 4000;
    private static final String IP_LOCALHOST = "127.0.0.1";

    private static final String END_OF_MESSAGE = "<END>";
    private static final String ERROR_MESSAGE = "<ERROR>";

    private static Socket server;

    public static void main(String[] args) {
        try {
            serverConnect();

            PrintStream output = new PrintStream(server.getOutputStream());

            ClientUI clientUI = new SimpleChatPanel(output);

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    processReceiveServerMessage(clientUI);
                } catch (IOException ioException) {
                    throw new RuntimeException(ioException);
                }
            });

            clientUI.onClose(() -> {
                try {
                    output.println("/exit\n<END>");
                    output.close();
                    server.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception exception) {
            System.out.println("Erro: " + exception.getMessage() + ", Abortando servidor...");
        }
    }

    private static void processReceiveServerMessage(ClientUI clientUI) throws IOException {
        Scanner input = new Scanner(server.getInputStream());

        try {
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
                    message.append(line + "\n");
                    line = input.nextLine();
                }

                clientUI.receiveMessage(from, message.toString(), isError);
            }
        } catch (Exception exception) {
            System.out.println("Houve um problema de conexão com o servidor, Erro: " + exception.getMessage());
        } finally {
            input.close();
            server.close();
        }
    }

    private static void serverConnect() throws IOException {
        try {
            server = new Socket(IP_LOCALHOST, SERVER_PORT_NUMBER);
            System.out.println("Conectado com o servidor com sucesso");
        } catch (Exception exception) {
            System.out.println("Erro ao conectar com servidor, erro: " + exception.getMessage());
            throw exception;
        }
    }
}
