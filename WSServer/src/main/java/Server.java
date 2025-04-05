import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT_NUMBER = 4000;

    public static final List<Client> clientList = new ArrayList<>();
    private static final String END_OF_MESSAGE = "\n<END>";

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(PORT_NUMBER);
            System.out.println("Servidor rodando na porta : " + PORT_NUMBER);

            while (!server.isClosed()) {
                Client client = new Client(server.accept());

                clientList.add(client);
                LogUtils.logNewConnection(client.getIp());
                processSendClientMessage(client, client.getIp(), CommandsValidator.SERVER_MESSAGE_IDENTIFIER + CommandsValidator.buildAvailableCommands());

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

                CommandsValidator.processAndValidateCommand(client, message);
            }
        } catch (Exception e) {
            System.out.println("Houve um problema de conexão com o cliente de ip " + client.getIp() + ", Erro: " + e.getMessage());
        }
    }

    public static void processSendClientMessage(Client client, String ip, String message) {
        try {
            PrintStream output = new PrintStream(client.getSocket().getOutputStream());

            output.println(message + END_OF_MESSAGE);
        } catch (Exception e) {
            System.out.println("Houve um problema ao enviar mensagem para o cliente de ip " + ip + ", Erro: " + e.getMessage());
        }
    }
}
