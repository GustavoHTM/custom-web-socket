package org.communication;

import java.util.Arrays;
import java.util.List;

public enum CommandEnum {

    USERS("/users", List.of("")),
    CHOOSE_NAME("/choose-name", List.of("<your-name>")),
    SEND_MESSAGE("/send-message", List.of("<user-name>", "<message>")),
    SEND_FILE("/send-file", List.of("<user-name>", "<file-path>")),
    EXIT("/exit", List.of(""));

    private final String command;
    private final List<String> arguments;

    CommandEnum(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public String getCommand() {
        return command;
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


    public String getCommandHelp() {
        return this.command + " " + String.join(" ", this.arguments);
    }

}
