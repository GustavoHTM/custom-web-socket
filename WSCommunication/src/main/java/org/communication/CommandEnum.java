package org.communication;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public enum CommandEnum {

    USERS("/users", List.of("")),
    CHOOSE_NAME("/choose-name", List.of("<your-name>")),
    SEND_MESSAGE("/send-message", List.of("<user-name>", "<message>")),
    SEND_FILE("/send-file", List.of("<user-name>", "<file-path>")),
    DOWNLOAD_FILE("/download-file", List.of("<user-name>", "<filename>")),
    EXIT("/exit", List.of("")),

    // Client side exclusive
    CLEAR("/clear", List.of(""));

    @Getter
    private final String command;

    @Getter
    private final List<String> arguments;

    CommandEnum(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public static CommandEnum convert(String command) {
        return Arrays.stream(CommandEnum.values())
            .filter(commandEnum -> commandEnum.command.equals(command) )
            .findFirst()
            .orElse(null);
    }

    public static List<CommandEnum> listUserCommands() {
        return Arrays.asList(
            USERS,
            SEND_MESSAGE,
            SEND_FILE,
            EXIT
        );
    }

    public static String generateCommand(CommandEnum commandEnum, String... args) {
        String command = commandEnum.getCommandHelp();

        if (commandEnum.arguments.size() != args.length) {
            return command;
        }

        for (int index = 0; index < commandEnum.arguments.size(); index++) {
            String argumentValue = args[index];
            if (argumentValue == null) continue;

            String argumentName = commandEnum.arguments.get(index);
            command = command.replace(argumentName, argumentValue);
        }

        return command;
    }

    public String getCommandHelp() {
        return this.command + " " + String.join(" ", this.arguments);
    }

}
