package org.client.ui;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;

import org.client.Client;
import org.communication.Message;
import org.communication.enums.CommandEnum;
import org.communication.utils.FileUtils;

public class CommandLineInterface {

    private final Client client;

    private final Scanner input;

    private final PrintStream output;

    private final Path receiveFilesPath = Paths.get(FileUtils.getDownloadsPath());

    public CommandLineInterface(Client client) {
        this.client = client;
        this.input = new Scanner(System.in);
        this.output = System.out;

        client.setLogged(false);

        chooseName();

        Executors.newSingleThreadExecutor().execute(() -> {
            client.listenMessages(() -> receiveFilesPath, this::receiveMessage);
        });

        while (this.input.hasNextLine()) {
            String message = this.input.nextLine();
            handleInput(message);
        }
    }

    private void chooseName() {
        System.out.print("Informe um nome de usuário: ");
        String name = this.input.nextLine();

        Message message = client.chooseName(name);
        receiveMessage(message);

        if (message.getType().isError()) {
            chooseName();
        }
    }

    private void handleInput(String message) {
        if (CommandEnum.CLEAR.getCommand().equals(message)) {
            clearScreen();
            return;
        }

        if (message.startsWith(CommandEnum.EXIT.getCommand())) {
            close();
        }

        client.sendMessage(message);
    }

    private void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception ignored) {}
    }

    private void receiveMessage(Message message) {
        this.output.println(message.getFrom() + ": " + message.getContent());

        if (message.getType().isSendFile()) {
            String[] args = message.getContent().split(" ");

            String[] subArray = Arrays.copyOfRange(args, 2, args.length);
            String filename = String.join(" ", subArray);

            this.client.sendMessage(CommandEnum.DOWNLOAD_FILE.buildCommand(message.getFrom(), filename));
        }
    }

    private void close() {
        this.client.close();
        this.input.close();
        this.output.close();

        System.exit(0);
    }

}
