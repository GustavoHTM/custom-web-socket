package org.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {

    private static final String LOG_FILE = "logfile.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void logNewConnection(String clientIp) {
        String date = LocalDateTime.now().format(FORMATTER);
        String message = "[" + date + "] Conexão estabelecida, client ip: " + clientIp;

        System.out.println(message);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (Exception exception) {
            System.err.println("Erro ao escrever no log: " + exception);
        }
    }
}
