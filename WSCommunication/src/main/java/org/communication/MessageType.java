package org.communication;

import java.util.Arrays;

public enum MessageType {

    MESSAGE("<MESSAGE>"),
    ERROR("<ERROR>"),
    FILE("<FILE>");

    private final String code;

    MessageType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static MessageType convert(String code) {
        return Arrays.stream(MessageType.values())
            .filter(messageType -> messageType.code.equals(code) )
            .findFirst()
            .orElse(null);
    }

}
