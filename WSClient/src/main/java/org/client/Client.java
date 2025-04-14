package org.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.communication.Message;
import org.communication.MessageBuilder;

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
                SimpleChatPanel simpleChatPanel = new SimpleChatPanel(output);

                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        processReceiveServerMessage(simpleChatPanel);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }, output);

        } catch (Exception exception) {
            System.out.println("Erro: " + exception.getMessage() + ", Abortando...");
        }
    }

    private static void processReceiveServerMessage(SimpleChatPanel simpleChatPanel) throws IOException {

        try (Scanner input = new Scanner(server.getInputStream())) {
            while (input.hasNextLine()) {
                Message message = MessageBuilder.buildMessage(input);
                if (message == null) continue;

                simpleChatPanel.receiveMessage(message);
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
