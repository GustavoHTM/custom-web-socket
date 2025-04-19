package org.communication.enums;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum MessageType {

    MESSAGE("<MESSAGE>"),
    ERROR("<ERROR>"),
    SEND_FILE("<SEND_FILE>"),
    RECEIVE_FILE("<RECEIVE_FILE>");

    private final String code;

    private MessageType(String code) {
        this.code = code;
    }

    public boolean isError() {
        return this == ERROR;
    }

    public boolean isSendFile() {
        return this == SEND_FILE;
    }

    public boolean isReceivedFile() {
        return this == RECEIVE_FILE;
    }

    public static MessageType convert(String code) {
        return Arrays.stream(MessageType.values())
            .filter(messageType -> messageType.code.equals(code))
            .findFirst()
            .orElse(null);
    }

}
