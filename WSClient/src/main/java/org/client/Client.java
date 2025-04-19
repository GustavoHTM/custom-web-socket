package org.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.client.ui.NameSelectorFrame;
import org.communication.IOCommunication;
import org.communication.Message;
import org.communication.enums.CommandEnum;
import org.communication.enums.MessageType;
import org.communication.handlers.MessageListener;

import lombok.Getter;
import lombok.NonNull;

public class Client {

    private static final String DEFAULT_LOCAL_SERVER_ADDRESS = "127.0.0.1:4000";

    private final IOCommunication ioCommunication;

    @Getter
    private String name = "undefined";

    private Client(Socket server) throws IOException {
        this.ioCommunication = new IOCommunication(server);
    }

    public static void main(String[] args) {
        try {
            String serverAddress = findServerAddress(args);
            String[] serverAddressParts = serverAddress.split(":");

            String ip = serverAddressParts[0];
            int port = Integer.parseInt(serverAddressParts[1]);

            Socket server = new Socket(ip, port);
            Client client = new Client(server);

            System.out.println("Conectado com o servidor " + ip + ":" + port);

            new NameSelectorFrame(client);
        } catch (Exception exception) {
            System.out.println("Erro: " + exception + ", Abortando...");
        }
    }

    public Message chooseName(String name) {
        sendMessage(CommandEnum.CHOOSE_NAME.buildCommand(name));

        Message message = listenMessage();
        if (!message.getType().isError()) {
            this.name = name;
        }

        return message;
    }

    public void sendMessage(String content) {
        this.ioCommunication.sendMessage(this.name, content);

        if (content.startsWith(CommandEnum.SEND_FILE.getCommand())) {
            String[] commandParts = content.split(" ");
            String filePath = String.join(" ", Arrays.copyOfRange(commandParts, 2, commandParts.length));

            this.ioCommunication.sendFile(filePath);
        }
    }

    public Message listenMessage() {
        return this.ioCommunication.waitSingleMessageReceive();
    }

    public void listenMessages(Supplier<Path> path, @NonNull MessageListener messageListener) {
        this.ioCommunication.waitMessageReceive((Message message) -> {
            if (!message.getType().isReceivedFile()) {
                messageListener.onMessageReceived(message);
                return;
            }

            if (this.ioCommunication.receiveFile(path.get())) {
                messageListener.onMessageReceived(new Message("SERVER", "Arquivo baixado com sucesso!"));
                return;
            }

            messageListener.onMessageReceived(new Message(MessageType.ERROR, "SERVER", "Erro ao baixar o arquivo"));
        });
    }

    public void receiveFile(String from, String filename) {
        sendMessage(CommandEnum.DOWNLOAD_FILE.buildCommand(from, filename));
    }

    public void close() {
        try {
            sendMessage(CommandEnum.EXIT.buildCommand());
            this.ioCommunication.close();
        } catch (Exception exception) {
            System.out.println("Erro ao encerrar concexão com servidor: " + exception);
        }

        System.exit(0);
    }

    private static String findServerAddress(String[] args) {
        if (args.length == 0) return DEFAULT_LOCAL_SERVER_ADDRESS;

        String inputAddress = args[0];
        Matcher matcher = Pattern.compile("^[^:\\s]+:(\\d{1,5})$").matcher(inputAddress);

        return matcher.matches() ? inputAddress : DEFAULT_LOCAL_SERVER_ADDRESS;
    }

}
