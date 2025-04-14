package org.communication;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum MessageType {

    MESSAGE("<MESSAGE>"),
    ERROR("<ERROR>"),
    FILE("<FILE>");

    @Getter
    private final String code;

    private MessageType(String code) {
        this.code = code;
    }

    public boolean isError() {
        return this == ERROR;
    }

    public static MessageType convert(String code) {
        return Arrays.stream(MessageType.values())
            .filter(messageType -> messageType.code.equals(code) )
            .findFirst()
            .orElse(null);
    }

}
