import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Client {

    private static final int SERVER_PORT_NUMBER = 4000;
    private static final String IP_LOCALHOST = "127.0.0.1";

    private static Socket server;

    public static void main(String[] args) {

        try {
            serverConnect();

            Scanner input = new Scanner(System.in);
            PrintStream output = new PrintStream(server.getOutputStream());

            Executors.newSingleThreadExecutor().execute(Client::processReceiveServerMessage);

            while (input.hasNextLine()) {
                output.println(input.nextLine());
            }

            output.close();
            input.close();
            server.close();
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage() + ", Abortando servidor...");
        }
    }

    private static void processReceiveServerMessage() {
        try {
            Scanner input = new Scanner(server.getInputStream());

            while (input.hasNextLine()) {
                String message = input.nextLine();
                System.out.println("Messagem do servidor recebida, lendo: " + message);
            }

            input.close();
            server.close();
        } catch (Exception e) {
            System.out.println("Houve um problema de conexão com o servidor, Erro: " + e.getMessage());
        }
    }

    private static void serverConnect() {
        try {
            server = new Socket(IP_LOCALHOST, SERVER_PORT_NUMBER);
            System.out.println("Conectado com o servidor com sucesso");
        } catch (Exception e) {
            System.out.println("Erro ao conectar com servidor, erro: " + e.getMessage());
        }
    }
}
