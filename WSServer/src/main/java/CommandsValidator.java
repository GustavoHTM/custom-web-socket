import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class CommandsValidator {

    private static final String INVALID_COMMAND = "Comando inválido.";
    private static final String INVALID_NAME = "Nome inválido.";
    private static final String INVALID_MESSAGE = "Mensagem inválida.";
    private static final String USER_NOT_FOUND = "Usuário não encontrado.";
    private static final String NAME_ALREADY_IN_USE = "Nome já está em uso.";
    private static final String SEND_FILE_ERROR = "Erro ao enviar arquivo para %s.";

    private static final String ERROR_IDENTIFIER = "<ERROR>\n";
    public static final String SERVER_MESSAGE_IDENTIFIER = "SERVER\n";

    private static final Map<String, String> availableCommandsWithExample = Map.of(
            "/users", "/users",
            "/choose-name", "/choose-name <your-name>",
            "/send-message", "/send-message <user-name> <mensagem>",
            "/send-file", "/send-file <user-name> <file-path>",
            "/sair", "/sair"
    );

    private static final Map<String, Integer> mapCodeErrors = Map.of(
            INVALID_COMMAND, 100,
            INVALID_NAME, 101,
            INVALID_MESSAGE, 102,
            USER_NOT_FOUND, 103,
            NAME_ALREADY_IN_USE, 104,
            SEND_FILE_ERROR, 105
    );

    static void processAndValidateCommand(Client client, String message) {
        String[] args = message.split(" ");
        if (args.length < 1) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(INVALID_COMMAND));
            return;
        }

        String command = args[0];
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (command) {
            case "/users":
                processShowUsersCommand(client);
                break;
            case "/choose-name":
                processChooseNameCommand(client, commandArgs);
                break;
            case "/send-message":
                processSendMessageCommand(client, commandArgs);
                break;
            case "/send-file":
                processSendFileCommand(client, commandArgs);
                break;
            case "/sair":
                processExitCommand(client);
                break;
            default:
                Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(INVALID_COMMAND));
        }
    }

    public static String buildAvailableCommands() {
        StringBuilder availableCommands = new StringBuilder("Comandos disponíveis:\n");
        for (Map.Entry<String, String> entry : availableCommandsWithExample.entrySet()) {
            if (entry.getKey().equals("/choose-name")) {
                continue;
            }

            availableCommands.append(entry.getValue()).append("\n");
        }

        return availableCommands.toString();
    }

    private static String buildErrorMessage(String errorMessage) {
        return ERROR_IDENTIFIER + "[" + mapCodeErrors.get(errorMessage) + "]" + errorMessage + "\n\n" + buildAvailableCommands();
    }

    private static void processShowUsersCommand(Client client) {
        StringBuilder users = new StringBuilder("Usuários conectados:\n");
        for (Client c : Server.clientList) {
            users.append(c.getName()).append("\n");
        }

        Server.processSendClientMessage(client, client.getIp(), SERVER_MESSAGE_IDENTIFIER + users);
    }

    private static void processChooseNameCommand(Client client, String[] args) {
        if (args.length < 1 || args[0].isEmpty()) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(INVALID_NAME));
            return;
        }

        String name = Arrays.asList(args).subList(0, args.length).stream().reduce((a, b) -> a + " " + b).orElse("");

        if (Server.clientList.stream().anyMatch(c -> c.getName().equalsIgnoreCase(name) && !client.getName().equalsIgnoreCase(name))) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(NAME_ALREADY_IN_USE));
            return;
        }

        client.setName(name);
    }

    private static void processSendMessageCommand(Client client, String[] args) {
        if (args.length < 2 || args[0].isEmpty() || args[1].isEmpty()) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(INVALID_MESSAGE));
            return;
        }

        String userName = args[0];
        String message = Arrays.asList(args).subList(1, args.length).stream().reduce((a, b) -> a + " " + b).orElse("");

        Client recipient = Server.clientList.stream()
                .filter(c -> c.getName().equalsIgnoreCase(userName))
                .findFirst()
                .orElse(null);

        if (recipient == null || client.getName().equalsIgnoreCase(userName)) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(USER_NOT_FOUND));
            return;
        }

        Server.processSendClientMessage(recipient, recipient.getIp(), client.getName() + "\n" + message);
    }

    private static void processSendFileCommand(Client client, String[] args) {
        if (args.length < 2 || args[1].isEmpty()) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(INVALID_NAME));
            return;
        }

        String targetName = args[1];
        String fileName = args[2];

        Client destinatario = Server.clientList.stream()
                .filter(c -> c.getName().equalsIgnoreCase(targetName))
                .findFirst()
                .orElse(null);

        if (destinatario == null) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(USER_NOT_FOUND));
            return;
        }

        try {
            DataInputStream dataIn = new DataInputStream(client.getSocket().getInputStream());

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

            DataOutputStream dataOut = new DataOutputStream(destinatario.getSocket().getOutputStream());

            dataOut.writeUTF("Arquivo recebido de " + client.getName() + ": " + fileName);
            dataOut.writeUTF(fileName);
            dataOut.writeLong(fileSize);
            dataOut.write(fileData);
            dataOut.flush();

            Server.processSendClientMessage(client, client.getIp(), "Arquivo enviado com sucesso para " + targetName);
        } catch (IOException e) {
            Server.processSendClientMessage(client, client.getIp(), buildErrorMessage(String.format(SEND_FILE_ERROR, targetName)));
            System.out.println("Erro ao enviar arquivo para " + targetName + ", Erro: " + e.getMessage());
        }
    }

    private static void processExitCommand(Client client) {

        try {
            Server.processSendClientMessage(client, client.getIp(), SERVER_MESSAGE_IDENTIFIER + "Conexão encerrada.");
            client.getSocket().close();

            System.out.println("Cliente de ip " + client.getIp() + " desconectado.");
        } catch (Exception e) {
            System.out.println("Erro ao fechar a conexão com o cliente de ip " + client.getIp() + ", Erro: " + e.getMessage());
        } finally {
            Server.clientList.remove(client);
        }
    }
}
