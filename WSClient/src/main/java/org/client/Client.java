package org.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import org.client.ui.NameSelectorFrame;
import org.communication.Message;
import org.communication.MessageType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private static final String DEFAULT_LOCAL_SERVER_ADDRESS = "127.0.0.1:4000";
    private static final Pattern SERVER_ADDRESS_PATTERN = Pattern.compile("^[^:\\s]+:(\\d{1,5})$");

    public static Socket server;
    private static PrintStream output;

    public static void main(String[] args) {
        try {
            String serverAddress = loadServerAddress(args);

            serverConnect(serverAddress);
            output = new PrintStream(server.getOutputStream(), true);

            new NameSelectorFrame(output);
        } catch (Exception exception) {
            System.out.println("Erro: " + exception.getMessage() + ", Abortando...");
        }
    }

    private static void serverConnect(String serverAddress) throws IOException {
        try {
            String[] parts = serverAddress.split(":");
            String ip = parts[0];
            int port = Integer.parseInt(parts[1]);

            server = new Socket(ip, port);
            output = new PrintStream(server.getOutputStream());
            System.out.println("Conectado com o servidor " + ip + ":" + port);
        } catch (Exception exception) {
            System.out.println("Erro ao conectar com servidor, erro: " + exception.getMessage());
            throw exception;
        }
    }

    public static void closeConnection() throws IOException {
        if (server != null && !server.isClosed()) {
            try {
                Message message = new Message(MessageType.MESSAGE, "Client", "/exit");
                output.println(message);
            } finally {
                output.close();
                server.close();
            }
        }

        System.exit(0);
    }

    private static String loadServerAddress(String[] args) {
        String inputAddress = args.length > 0 ? args[0] : DEFAULT_LOCAL_SERVER_ADDRESS;

        Matcher matcher = SERVER_ADDRESS_PATTERN.matcher(inputAddress);
        return matcher.matches() ? inputAddress : DEFAULT_LOCAL_SERVER_ADDRESS;
    }
}
