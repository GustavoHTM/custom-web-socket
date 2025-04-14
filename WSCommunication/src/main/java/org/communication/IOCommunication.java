package org.communication;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.communication.handler.MessageListener;

import lombok.NonNull;

public class IOCommunication {

    private final String name;

    public IOCommunication(@NonNull String name) {
        this.name = name;
    }

    public void waitMessageReceive(InputStream inputStream, @NonNull MessageListener messageListener) {
        try (Scanner input = new Scanner(inputStream)) {
            while (input.hasNextLine()) {
                Message message = MessageBuilder.buildMessage(input);

                if (message != null) {
                    messageListener.onMessageReceived(message);
                }
            }
        } catch (Exception exception) {
            System.out.println("Houve um problema de conexão com o servidor, Erro: " + exception.getMessage());
        }
    }

    public Message waitSingleMessageReceive(InputStream inputStream) {
        try (Scanner input = new Scanner(inputStream)) {
            if (input.hasNextLine()) {
                Message message = MessageBuilder.buildMessage(input);

                if (message != null) {
                    return message;
                }
            }
        } catch (Exception exception) {
            System.out.println("Houve um problema de conexão com o servidor, Erro: " + exception.getMessage());
        }

        return null;
    }

    public void sendMessage(PrintStream output, String content) {
        this.sendMessage(output, MessageType.MESSAGE, content);
    }

    public void sendMessage(PrintStream output, MessageType type, String content) {
        Message message = new Message(type, this.name, content);

        output.println(message);
    }
}
