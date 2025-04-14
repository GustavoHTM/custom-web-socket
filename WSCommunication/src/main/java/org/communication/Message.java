package org.communication;

import lombok.Getter;

@Getter
public class Message {

    public static final String END_MESSAGE = "<END>";
    public static final String NEW_LINE = "\n";

    private String from;

    private String content;

    private MessageType type;

    public Message(MessageType type, String from, String content) {
        this.from = from;
        this.content = content;
        this.type = type;
    }

    public String buildMessage() {
        StringBuilder message = new StringBuilder();

        message.append(this.type.getCode());
        message.append(NEW_LINE);
        message.append(this.from);
        message.append(NEW_LINE);
        message.append(this.content);
        message.append(NEW_LINE);
        message.append(END_MESSAGE);

        return message.toString();
    }

}
