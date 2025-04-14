package org.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import org.client.ui.NameSelectorFrame;
import org.communication.Message;
import org.communication.MessageType;

// todo: Inverter dependencia, fazer chat instanciar o client ou receber uma instancia de client
public class Client {

    private static final int SERVER_PORT_NUMBER = 4000;
    private static final String IP_LOCALHOST = "127.0.0.1";

    public static Socket server;
    private static PrintStream output;

    public static void main(String[] args) {
        try {
            serverConnect();
            output = new PrintStream(server.getOutputStream(), true);

            new NameSelectorFrame(output);
        } catch (Exception exception) {
            System.out.println("Erro: " + exception.getMessage() + ", Abortando...");
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
}
