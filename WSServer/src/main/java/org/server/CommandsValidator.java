package org.server;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.communication.Message;
import org.communication.enums.CommandEnum;
import org.communication.enums.ErrorEnum;
import org.communication.enums.MessageType;
import org.communication.utils.FileUtils;

public class CommandsValidator {

    public static final String SERVER_MESSAGE_IDENTIFIER = "SERVER";

    private static final String FILES_DIRECTORY = FileUtils.getTempPath() + File.separator + "socket_files";

    static void validateAndProcessCommand(Client client, Message message) {
        CommandEnum commandEnum = CommandEnum.convert(message.getContent());
        if (commandEnum == null) {
            client.receiveMessage(buildErrorMessage(ErrorEnum.INVALID_COMMAND));
            return;
        }

        LinkedList<String> arguments = commandEnum.getArgumentValues();
        try {
            switch (commandEnum) {
                case USERS:
                    processShowUsersCommand(client);
                    break;
                case CHOOSE_NAME:
                    processChooseNameCommand(client, arguments);
                    break;
                case SEND_MESSAGE:
                    processSendMessageCommand(client, arguments);
                    break;
                case SEND_FILE:
                    processSendFileCommand(client, arguments);
                    break;
                case DOWNLOAD_FILE:
                    processDownloadFileCommand(client, arguments);
                    break;
                case EXIT:
                    processExitCommand(client);
                    break;
            }
        } catch (Exception exception) {
            System.out.println("processAndValidateCommand >> Erro ao processar comando: " + exception);
        }
    }

    public static String buildAvailableCommands() {
        return "Comandos disponíveis:\n" + CommandEnum.listUserCommands()
            .stream()
            .map(CommandEnum::toString)
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
        for (Client c : Server.CLIENT_LIST) {
            users.append(c.getName()).append("\n");
        }

        Message message = new Message(SERVER_MESSAGE_IDENTIFIER, users.toString());
        client.receiveMessage(message);
    }

    private static void processChooseNameCommand(Client client, LinkedList<String> arguments) {
        if (!isValidChooseName(arguments)) {
            client.receiveMessage(buildSimpleErrorMessage(ErrorEnum.INVALID_NAME));
        }

        String newName = String.join(" ", arguments);
        if (!isValidNewName(client.getName(), newName)) {
            client.receiveMessage(buildSimpleErrorMessage(ErrorEnum.NAME_ALREADY_IN_USE));
            return;
        }

        client.setName(newName);

        String message = "Olá " + newName + "\n\n" + buildAvailableCommands();
        client.receiveMessage(SERVER_MESSAGE_IDENTIFIER, message);
    }

    private static boolean isValidChooseName(LinkedList<String> arguments) {
        if (arguments.isEmpty()) return false;
        return !arguments.getFirst().isEmpty();
    }

    private static boolean isValidNewName(String currentName, String newName) {
        if (currentName.equalsIgnoreCase(newName)) {
            return false;
        }

        return Server.findClient(newName) == null;
    }

    private static void processSendMessageCommand(Client sender, LinkedList<String> arguments) {
        if (!isValidSendMessage(arguments)) {
            sender.receiveMessage(buildErrorMessage(ErrorEnum.INVALID_MESSAGE));
            return;
        }

        String receiverName = arguments.removeFirst();
        if (sender.getName().equalsIgnoreCase(receiverName)) {
            sender.receiveMessage(buildErrorMessage(ErrorEnum.USER_NOT_FOUND));
            return;
        }

        Client receiver = Server.findClient(receiverName);
        if (receiver == null) {
            sender.receiveMessage(buildErrorMessage(ErrorEnum.USER_NOT_FOUND));
            return;
        }

        String message = String.join(" ", arguments);
        receiver.receiveMessage(sender.getName(), message);
    }

    private static boolean isValidSendMessage(LinkedList<String> arguments) {
        if (arguments.size() < CommandEnum.SEND_MESSAGE.getArgumentNames().size()) {
            return false;
        }

        String receiverName = arguments.getFirst();
        if (receiverName.isEmpty()) {
            return false;
        }

        String messageStart = arguments.get(1);
        return !messageStart.isEmpty();
    }

    private static void processSendFileCommand(Client sender, LinkedList<String> arguments) {
        if (!isValidSendFile(arguments)) {
            sender.receiveMessage(buildErrorMessage(ErrorEnum.INVALID_NAME));
            return;
        }

        String receiverName = arguments.removeFirst();
        Client receiver = Server.findClient(receiverName);
        if (receiver == null) {
            sender.receiveMessage(buildErrorMessage(ErrorEnum.USER_NOT_FOUND));
            return;
        }

        String filename = Paths.get(String.join(" ", arguments)).getFileName().toString();
        Path path = Paths.get(FILES_DIRECTORY, receiverName, sender.getName());

        try {
            if (!sender.sendFile(path)) {
                sender.receiveMessage(buildSimpleErrorMessage(ErrorEnum.SEND_FILE_ERROR, receiverName));
            }

            Message successfulFileSentMessage = new Message(SERVER_MESSAGE_IDENTIFIER, "Arquivo enviado com sucesso para " + receiverName);
            sender.receiveMessage(successfulFileSentMessage);

            Message fileReceivedMessage = new Message(MessageType.SEND_FILE, sender.getName(), "Arquivo recebido: " + filename);
            receiver.receiveMessage(fileReceivedMessage);
        } catch (Exception exception) {
            System.out.println("Ocorreu um erro ao salvar arquivo no servidor: " + exception);
            sender.receiveMessage(buildSimpleErrorMessage(ErrorEnum.SEND_FILE_ERROR, receiverName));
        }
    }

    private static boolean isValidSendFile(LinkedList<String> arguments) {
        if (arguments.size() < CommandEnum.SEND_FILE.getArgumentNames().size()) {
            return false;
        }

        String receiverName = arguments.getFirst();
        if (receiverName.isEmpty()) {
            return false;
        }

        String filenameStart = arguments.get(1);
        return !filenameStart.isEmpty();
    }

    private static void processDownloadFileCommand(Client receiver, LinkedList<String> arguments) {
        if (!isValidDownloadFile(arguments)) {
            receiver.receiveMessage(buildErrorMessage(ErrorEnum.INVALID_NAME));
            return;
        }

        String senderName = arguments.removeFirst();
        Client sender = Server.findClient(senderName);
        if (sender == null) {
            receiver.receiveMessage(buildErrorMessage(ErrorEnum.USER_NOT_FOUND));
            return;
        }

        String filename = String.join(" ", arguments);
        Path filepath = Paths.get(FILES_DIRECTORY, receiver.getName(), senderName, filename);

        if (!filepath.toFile().exists()) {
            sender.receiveMessage(buildSimpleErrorMessage(ErrorEnum.FILE_NOT_FOUND));
            return;
        }

        Message receiveingFileMessage = new Message(MessageType.RECEIVE_FILE, SERVER_MESSAGE_IDENTIFIER, "");
        receiver.receiveFile(receiveingFileMessage, filepath.toString());
    }

    private static boolean isValidDownloadFile(LinkedList<String> arguments) {
        if (arguments.size() < CommandEnum.DOWNLOAD_FILE.getArgumentNames().size()) {
            return false;
        }

        String senderName = arguments.getFirst();
        if (senderName.isEmpty()) {
            return false;
        }

        String filenameStart = arguments.get(1);
        return !filenameStart.isEmpty();
    }

    private static void processExitCommand(Client client) {
        try {
            client.receiveMessage(SERVER_MESSAGE_IDENTIFIER, "Conexão encerrada.");
            client.close();

            System.out.println("Cliente de ip " + client.getIp() + " desconectado.");
        } catch (Exception exception) {
            System.out.println("Erro ao fechar a conexão com o cliente de ip " + client.getIp() + ", Erro: " + exception);
        } finally {
            Server.removeClient(client);
        }
    }

}
