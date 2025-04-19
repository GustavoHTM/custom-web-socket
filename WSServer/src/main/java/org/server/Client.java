package org.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;

import org.communication.IOCommunication;
import org.communication.Message;
import org.server.handlers.ServerMessageListener;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
public class Client {

    private final String ip;

    private final IOCommunication ioCommunication;

    @Setter
    private String name;

    public Client(Socket socket) throws IOException {
        this.ip = socket.getInetAddress().getHostAddress();
        this.ioCommunication = new IOCommunication(socket);
        this.name = this.ip;
    }

    public void receiveMessage(Message message) {
        this.ioCommunication.sendMessage(message);
    }

    public void receiveMessage(String from, String content) {
        this.ioCommunication.sendMessage(from, content);
    }

    public void sendMessages(@NonNull ServerMessageListener serverMessageListener) {
        this.ioCommunication.waitMessageReceive(message -> serverMessageListener.onMessageReceived(this, message));
    }

    public void receiveFile(Message receivingFileMessage, String filePath) {
        receiveMessage(receivingFileMessage);
        this.ioCommunication.sendFile(filePath);
    }

    public boolean sendFile(Path path) {
        return this.ioCommunication.receiveFile(path);
    }

    public void close() throws IOException {
        this.ioCommunication.close();
    }

}
