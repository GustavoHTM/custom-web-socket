package org.communication;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.communication.enums.MessageType;
import org.communication.handlers.MessageListener;

import lombok.NonNull;

public class IOCommunication {

    private final Socket socket;

    private final InputStream inputStream;

    private final Scanner input;

    private final PrintStream output;

    public IOCommunication(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.input = new Scanner(this.inputStream);
        this.output = new PrintStream(socket.getOutputStream(), true);
    }

    public void waitMessageReceive(@NonNull MessageListener messageListener) {
        while (!this.socket.isClosed() && this.input.hasNextLine()) {
            try {
                Message message = MessageBuilder.buildMessage(input);

                if (message != null) {
                    System.out.println(">> Recebendo mensagem:\n" + message.getContent());
                    messageListener.onMessageReceived(message);
                }
            } catch (Exception exception) {
                System.out.println("waitMessageReceive >> Houve um problema de conexão com o servidor, Erro: " + exception);
            }
        }
    }

    public Message waitSingleMessageReceive() {
        try {
            if (this.input.hasNextLine()) {
                Message message = MessageBuilder.buildMessage(input);
                if (message != null) {
                    System.out.println(">> Recebendo mensagem:\n" + message.getContent());
                    return message;
                }
            }
        } catch (Exception exception) {
            System.out.println("waitSingleMessageReceive >> Houve um problema de conexão com o servidor, Erro: " + exception);
        }

        return null;
    }

    public void sendMessage(String from, String content) {
        sendMessage(new Message(from, content));
    }

    public void sendMessage(Message message) {
        this.output.println(message);
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

    public byte[] receiveFile(InputStream inputStream, Path receivePath, String filename) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            DataInputStream dataIn = new DataInputStream(inputStream);

            if (bufferedReader.ready()) {
                String possibleError = bufferedReader.readLine();

                if (MessageType.ERROR.name().equals(possibleError)) {
                    System.out.println("Ocorreu um erro no recebimento do arquivo.");
                    return null;
                }
            }

            long fileSize = dataIn.readLong();

            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while (totalRead < fileSize && (bytesRead = dataIn.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                baos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }

            byte[] fileData = baos.toByteArray();

            if (Files.notExists(receivePath)) {
                Files.createDirectories(receivePath);
            }

            Path newFilepath = Paths.get(receivePath.toString(), filename);

            FileOutputStream fileOutputStream = new FileOutputStream(newFilepath.toString());
            fileOutputStream.write(fileData);
            fileOutputStream.close();
            System.out.println("Arquivo criado com sucesso!");

            return fileData;
        } catch (IOException e) {
            System.out.println("Erro ao enviar arquivo, Erro: " + e.getMessage());
            return null;
        }
    }

    public void close() throws IOException {
        if (this.socket.isClosed()) return;

        this.output.close();
        this.input.close();
        this.socket.close();
    }

}
