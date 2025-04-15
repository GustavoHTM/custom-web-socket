package org.server;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.communication.IOCommunication;
import org.communication.Message;
import org.communication.MessageBuilder;

public class Server {

    private static final int PORT_NUMBER = 4000;

    public static final List<Client> clientList = new ArrayList<>();
    public static final IOCommunication ioCommunication = IOCommunication.getInstance(CommandsValidator.SERVER_MESSAGE_IDENTIFIER);

    public static void main(String[] args) {

        try {
            ServerSocket server = new ServerSocket(PORT_NUMBER);
            System.out.println("Servidor rodando na porta : " + PORT_NUMBER);

            while (!server.isClosed()) {
                Client client = new Client(server.accept());

                clientList.add(client);
                LogUtils.logNewConnection(client.getIp());

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
                Message message = MessageBuilder.buildMessage(input);
                if (message == null) continue;

                System.out.println("Messagem do cliente ip: " + client.getIp() + " recebida, lendo: " + message.toString().trim());
                CommandsValidator.processAndValidateCommand(client, message.getContent());
            }
        } catch (Exception e) {
            System.out.println("Houve um problema de conexão com o cliente de ip " + client.getIp() + ", Erro: " + e.getMessage());
        }
    }

    public static void processSendClientMessage(Client client, Message message) {
        try {
            PrintStream output = new PrintStream(client.getSocket().getOutputStream());
            output.println(message);
        } catch (Exception e) {
            System.out.println("Houve um problema ao enviar mensagem para o cliente de ip " + client.getIp() + ", Erro: " + e.getMessage());
        }
    }
}
