package org.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Server {

    private static final int PORT_NUMBER = 4000;

    public static final List<Client> CLIENT_LIST = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT_NUMBER)) {
            System.out.println("Servidor rodando na porta : " + PORT_NUMBER);

            while (!server.isClosed()) {
                acceptNewUser(server);
            }
        } catch (Exception exception) {
            System.out.println("Erro inesperado: " + exception + "\n finalizando servidor");
        }
    }

    public static Client findClient(String clientName) {
        return CLIENT_LIST.stream()
            .filter(targetClient -> targetClient.getName().equalsIgnoreCase(clientName))
            .findFirst()
            .orElse(null);
    }

    public static void removeClient(Client client) {
        CLIENT_LIST.remove(client);
    }

    private static void acceptNewUser(ServerSocket server) {
        try {
            Client client = new Client(server.accept());
            CLIENT_LIST.add(client);

            LogUtils.logNewConnection(client.getIp());

            Executors.newSingleThreadExecutor().execute(() -> {
                client.sendMessages(CommandsValidator::validateAndProcessCommand);
            });
        } catch (Exception exception) {
            System.out.println("Erro ao criar conexão com cliente: " + exception);
        }
    }

}
