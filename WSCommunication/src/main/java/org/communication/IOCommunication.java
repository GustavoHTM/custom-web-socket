package org.communication;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.communication.handler.MessageListener;

import lombok.NonNull;

public class IOCommunication {

    private static String name;
    private static IOCommunication instance;

    private IOCommunication() { }

    public static synchronized IOCommunication getInstance(@NonNull String newName) {
        if (instance == null) {
            instance = new IOCommunication();
        }

        name = newName;

        return instance;
    }

    public void waitMessageReceive(InputStream inputStream, @NonNull MessageListener messageListener) {
        try {
            Scanner input = new Scanner(inputStream);
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
        try {
            Scanner input = new Scanner(inputStream);
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
        Message message = new Message(type, name, content);

        output.println(message);
    }

    public void sendFile(PrintStream output, String filePath) {
        DataOutputStream dataOutputStream;
        FileInputStream fileInputStream = null;

        try {
            File fileToSend = new File(filePath);

            if (!fileToSend.exists()) {
                fileToSend.createNewFile();
            }

            dataOutputStream = new DataOutputStream(output);
            fileInputStream = new FileInputStream(fileToSend);
            dataOutputStream.writeLong(fileToSend.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesSent = 0;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytesSent += bytesRead;

                double progress = (double) totalBytesSent / fileToSend.length() * 100;
                System.out.printf("Enviando: %s - Progresso: %.2f%%\n", fileToSend.getName(), progress);
            }
        } catch (Exception exception) {
            System.out.println("ERRO " + exception);
            output.println(MessageType.ERROR);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }
}
