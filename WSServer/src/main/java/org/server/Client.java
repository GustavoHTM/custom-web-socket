package org.server;

import java.io.PrintStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private String ip;
    private String name;
    private PrintStream output;

    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.name = this.ip;

        try {
            this.output = new PrintStream(socket.getOutputStream());
        } catch (Exception e) {
            System.out.println("Erro ao criar PrintStream para o cliente: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrintStream getOutput() {
        return output;
    }

    public void setOutput(PrintStream output) {
        this.output = output;
    }
}
