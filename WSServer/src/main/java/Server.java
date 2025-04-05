import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT_NUMBER = 4000;

    private static ServerSocket server;
    private static List<Socket> clientList = new ArrayList<>();

    private static final Map<String, String> availableCommandsWithExample = Map.of(
            "/users", "/users",
            "/choose-name", "/choose-name <your-name>",
            "/send-message", "/send-message <user-name> <mensagem>",
            "/send-file", "/send-file <user-name> <file-path>",
            "/show-history", "/show-history <user-name>",
            "/sair", "/sair"
    );

    public static void main(String[] args) {

        try {
            server = new ServerSocket(PORT_NUMBER);
            System.out.println("Servidor rodando na porta : " + PORT_NUMBER);

            while (!server.isClosed()) {
                Socket client = server.accept();
                String ip = client.getInetAddress().getHostAddress();

                clientList.add(client);
                System.out.println("Conexão estabelecida, client ip: " + ip);

                Executors.newSingleThreadExecutor().execute(() -> processReceiveClientMessage(client, ip));
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage() + ", Abortando servidor...");
        }
    }

    private static void processReceiveClientMessage(Socket client, String ip) {
        try {
            Scanner input = new Scanner(client.getInputStream());

            while (input.hasNextLine()) {
                String message = input.nextLine();
                System.out.println("Messagem do cliente ip: " + ip + " recebida, lendo: " + message);
                for (Socket socketClient : clientList) {
                    if (!client.equals(socketClient)) {
                        processSendClientMessage(socketClient, socketClient.getInetAddress().getHostAddress(), message);
                    }
                }
            }

            input.close();
            client.close();
        } catch (Exception e) {
            System.out.println("Houve um problema de conexão com o cliente de ip " + ip + ", Erro: " + e.getMessage());
        }
    }

    private static void processSendClientMessage(Socket client, String ip, String message) {
        try {
            PrintStream output = new PrintStream(client.getOutputStream());

            output.println(message);
            output.close();
        } catch (Exception e) {
            System.out.println("Houve um problema ao enviar mensagem para o cliente de ip " + ip + ", Erro: " + e.getMessage());
        }
    }
}
