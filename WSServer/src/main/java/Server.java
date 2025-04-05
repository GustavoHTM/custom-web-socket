import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT_NUMBER = 4000;

    private static final List<Client> clientList = new ArrayList<>();

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
            ServerSocket server = new ServerSocket(PORT_NUMBER);
            System.out.println("Servidor rodando na porta : " + PORT_NUMBER);

            while (!server.isClosed()) {
                Client client = new Client(server.accept());

                clientList.add(client);
                System.out.println("Conexão estabelecida, client ip: " + client.getIp());

                Executors.newSingleThreadExecutor().execute(() -> processReceiveClientMessage(client));
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage() + ", Abortando servidor...");
        }
    }

    private static void processReceiveClientMessage(Client client) {
        try {
            Scanner input = new Scanner(client.getSocket().getInputStream());

            while (input.hasNextLine()) {
                String message = input.nextLine();
                System.out.println("Messagem do cliente ip: " + client.getIp() + " recebida, lendo: " + message);
                for (Client socketClient : clientList) {
                    if (!client.equals(socketClient)) {
                        processSendClientMessage(socketClient, socketClient.getIp(), message);
                    }
                }
            }

            input.close();
            client.getSocket().close();
        } catch (Exception e) {
            System.out.println("Houve um problema de conexão com o cliente de ip " + client.getIp() + ", Erro: " + e.getMessage());
        }
    }

    private static void processSendClientMessage(Client client, String ip, String message) {
        try {
            PrintStream output = new PrintStream(client.getSocket().getOutputStream());

            output.println(message);
        } catch (Exception e) {
            System.out.println("Houve um problema ao enviar mensagem para o cliente de ip " + ip + ", Erro: " + e.getMessage());
        }
    }
}
