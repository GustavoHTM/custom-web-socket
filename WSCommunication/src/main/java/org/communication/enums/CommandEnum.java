package org.communication.enums;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

@Getter
public enum CommandEnum {

    USERS("/users", List.of()),
    CHOOSE_NAME("/choose-name", List.of("<your-name>")),
    SEND_MESSAGE("/send-message", List.of("<user-name>", "<message>")),
    SEND_FILE("/send-file", List.of("<user-name>", "<file-path>")),
    DOWNLOAD_FILE("/download-file", List.of("<user-name>", "<filename>")),
    EXIT("/exit", List.of()),

    // Client side exclusive
    CLEAR("/clear", List.of(""));

    private static final String INVALID_ARGUMENT_VALUE = "<invalid_argument>";

    private final String command;

    private final List<String> argumentNames;

    private LinkedList<String> argumentValues;

    CommandEnum(String command, List<String> argumentNames) {
        this.command = command;
        this.argumentNames = argumentNames;
        this.argumentValues = new LinkedList<>(argumentNames);
    }


    public static CommandEnum convert(String command) {
        String[] args = command.trim().split(" ");
        if (args.length < 1) return null;

        CommandEnum commandEnum = findCommand(args[0].trim());
        if (commandEnum == null) return null;

        if ((args.length - 1) < commandEnum.argumentNames.size()) {
            return null;
        }

        commandEnum.argumentValues = new LinkedList<>(Arrays.asList(args).subList(1, args.length));

        return commandEnum;
    }

    public static List<CommandEnum> listUserCommands() {
        return Arrays.asList(
            USERS,
            SEND_MESSAGE,
            SEND_FILE,
            EXIT
        );
    }

    private static CommandEnum findCommand(String command) {
        return Arrays.stream(CommandEnum.values())
            .filter(commandEnum -> commandEnum.command.equals(command))
            .findFirst().orElse(null);
    }

    public String buildCommand(String... args) {
        if (args.length < this.argumentNames.size()) {
            return toString();
        }

        this.argumentValues = new LinkedList<>();
        for (int index = 0; index < args.length; index++) {
            String argumentValue = args[index];
            if (argumentValue == null) {
                if (index < this.argumentNames.size()) {
                    argumentValue = this.argumentNames.get(index);
                } else {
                    argumentValue = INVALID_ARGUMENT_VALUE;
                }
            }

            argumentValues.add(argumentValue);
        }

        return toString();
    }

    @Override
    public String toString() {
        String command = this.command + " " + String.join(" ", this.argumentValues);
        return command.trim();
    }

}
