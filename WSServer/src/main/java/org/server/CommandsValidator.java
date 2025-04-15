package org.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.communication.CommandEnum;
import org.communication.ErrorEnum;
import org.communication.FileUtils;
import org.communication.Message;
import org.communication.MessageType;

public class CommandsValidator {

    private static final String SERVER_MESSAGE_IDENTIFIER = "SERVER";

    private static final String FILES_DIRECTORY = FileUtils.getDesktopPath() + File.separator + "socket_files";

    static void processAndValidateCommand(Client client, String messageContent) {
        String[] args = messageContent.trim().split(" ");
        if (args.length < 1) {
            Server.processSendClientMessage(client, buildErrorMessage(ErrorEnum.INVALID_COMMAND));
            return;
        }

        String command = args[0].trim();
        CommandEnum commandEnum = CommandEnum.convert(command);

        if (commandEnum == null) {
            Server.processSendClientMessage(client, buildErrorMessage(ErrorEnum.INVALID_COMMAND));
            return;
        }

        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (commandEnum) {
            case USERS:
                processShowUsersCommand(client);
                break;
            case CHOOSE_NAME:
                processChooseNameCommand(client, commandArgs);
                break;
            case SEND_MESSAGE:
                processSendMessageCommand(client, commandArgs);
                break;
            case SEND_FILE:
                processSendFileCommand(client, commandArgs);
                break;
            case EXIT:
                processExitCommand(client);
                break;
        }
    }

    public static String buildAvailableCommands() {
        return "Comandos disponíveis:\n" + CommandEnum.listUserCommands()
            .stream()
            .map(CommandEnum::getCommandHelp)
            .collect(Collectors.joining("\n"));
    }

    private static Message buildSimpleErrorMessage(ErrorEnum errorEnum, String... args) {
        String errorMessageContent = "[" + errorEnum.getCode() + "] " + String.format(errorEnum.getDescriptor(), args);

        return new Message(MessageType.ERROR, SERVER_MESSAGE_IDENTIFIER, errorMessageContent);
    }

    private static Message buildErrorMessage(ErrorEnum errorEnum, String... args) {
        String errorMessageContent = "[" + errorEnum.getCode() + "] " + String.format(errorEnum.getDescriptor(), args) + "\n\n" + buildAvailableCommands();

        return new Message(MessageType.ERROR, SERVER_MESSAGE_IDENTIFIER, errorMessageContent);
    }

    private static void processShowUsersCommand(Client client) {
        StringBuilder users = new StringBuilder("Usuários conectados:\n");
        for (Client c : Server.clientList) {
            users.append(c.getName()).append("\n");
        }

        Message message = new Message(MessageType.MESSAGE, SERVER_MESSAGE_IDENTIFIER, users.toString());
        Server.processSendClientMessage(client, message);
    }

    private static void processChooseNameCommand(Client client, String[] args) {
        if (args.length < 1 || args[0].isEmpty()) {
            Server.processSendClientMessage(client, buildSimpleErrorMessage(ErrorEnum.INVALID_NAME));
            return;
        }

        String name = Arrays.asList(args).subList(0, args.length).stream().reduce((a, b) -> a + " " + b).orElse("");

        if (Server.clientList.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name) && !client.getName().equalsIgnoreCase(name))) {
            Server.processSendClientMessage(client, buildSimpleErrorMessage(ErrorEnum.NAME_ALREADY_IN_USE));
            return;
        }

        client.setName(name);

        String messageContent = "Olá " + name + "\n\n" + buildAvailableCommands();
        Message message = new Message(MessageType.MESSAGE, SERVER_MESSAGE_IDENTIFIER, messageContent);

        Server.processSendClientMessage(client, message);
    }

    private static void processSendMessageCommand(Client client, String[] args) {
        if (args.length < 2 || args[0].isEmpty() || args[1].isEmpty()) {
            Server.processSendClientMessage(client, buildErrorMessage(ErrorEnum.INVALID_MESSAGE));
            return;
        }

        String userName = args[0];
        String messageContent = Arrays.asList(args).subList(1, args.length).stream().reduce((a, b) -> a + " " + b).orElse("");

        Client recipient = Server.clientList.stream()
            .filter(c -> c.getName().equalsIgnoreCase(userName))
            .findFirst()
            .orElse(null);

        if (recipient == null || client.getName().equalsIgnoreCase(userName)) {
            Server.processSendClientMessage(client, buildErrorMessage(ErrorEnum.USER_NOT_FOUND));
            return;
        }

        Message message = new Message(MessageType.MESSAGE, client.getName(), messageContent);

        Server.processSendClientMessage(recipient, message);
    }

    private static void processSendFileCommand(Client client, String[] args) {
        if (args.length < 2 || args[1].isEmpty()) {
            Server.processSendClientMessage(client, buildErrorMessage(ErrorEnum.INVALID_NAME));
            return;
        }

        String targetName = args[0].trim();

        String[] subArray = Arrays.copyOfRange(args, 1, args.length);
        Path filepath = Paths.get(String.join(" ", subArray));

        Client targetClient = Server.clientList.stream()
            .filter(c -> c.getName().equalsIgnoreCase(targetName))
            .findFirst()
            .orElse(null);

        if (targetClient == null) {
            Server.processSendClientMessage(client, buildErrorMessage(ErrorEnum.USER_NOT_FOUND));
            return;
        }

        try {
            InputStream inputStream = client.getSocket().getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            DataInputStream dataIn = new DataInputStream(inputStream);

            if (bufferedReader.ready()) {
                String possibleError = bufferedReader.readLine();

                if (MessageType.ERROR.name().equals(possibleError)) {
                    System.out.println("Ocorreu um erro no envio do arquivo.");
                    Server.processSendClientMessage(client, buildSimpleErrorMessage(ErrorEnum.SEND_FILE_ERROR, targetName));
                    return;
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

            Path fileDirectory = Paths.get(FILES_DIRECTORY, targetName);
            if (Files.notExists(fileDirectory)) {
                Files.createDirectories(fileDirectory);
            }

            Path newFilepath = Paths.get(fileDirectory.toString(), filepath.getFileName().toString());

            try (FileOutputStream fileOutputStream = new FileOutputStream(newFilepath.toString())) {
                fileOutputStream.write(fileData);
                System.out.println("Arquivo criado com sucesso!");
            } catch (IOException e) {
                System.err.println("Erro ao criar arquivo: " + e.getMessage());
            }

            Message successfulFileSentMessage = new Message(MessageType.MESSAGE, SERVER_MESSAGE_IDENTIFIER, "Arquivo enviado com sucesso para " + targetName);
            Server.processSendClientMessage(client, successfulFileSentMessage);

            Message fileReceivedMessage = new Message(MessageType.FILE, client.getName(), "Arquivo recebido: " + filepath.getFileName().toString());
            Server.processSendClientMessage(targetClient, fileReceivedMessage);
        } catch (IOException e) {
            Server.processSendClientMessage(client, buildSimpleErrorMessage(ErrorEnum.SEND_FILE_ERROR, targetName));
            System.out.println("Erro ao enviar arquivo para " + targetName + ", Erro: " + e.getMessage());
        }
    }

    private static void processExitCommand(Client client) {
        try {
            String messageContent = "Conexão encerrada.";
            Message message = new Message(MessageType.MESSAGE, SERVER_MESSAGE_IDENTIFIER, messageContent);

            Server.processSendClientMessage(client, message);
            client.getSocket().close();

            System.out.println("Cliente de ip " + client.getIp() + " desconectado.");
        } catch (Exception e) {
            System.out.println("Erro ao fechar a conexão com o cliente de ip " + client.getIp() + ", Erro: " + e.getMessage());
        } finally {
            Server.clientList.remove(client);
        }
    }
}
