package org.communication.enums;

import lombok.Getter;

@Getter
public enum ErrorEnum {

    INVALID_COMMAND("Comando inválido.", 100),
    INVALID_NAME("Nome inválido.", 101),
    INVALID_MESSAGE("Mensagem inválida.", 102),
    USER_NOT_FOUND("Usuário não encontrado.", 103),
    NAME_ALREADY_IN_USE("Nome já está em uso.", 104),
    SEND_FILE_ERROR("Erro ao enviar arquivo para %s.", 105),
    FILE_NOT_FOUND("Arquivo não encontrado.", 106);

    private final String descriptor;
    private final int code;

    ErrorEnum(String descriptor, int code) {
        this.descriptor = descriptor;
        this.code = code;
    }
}
