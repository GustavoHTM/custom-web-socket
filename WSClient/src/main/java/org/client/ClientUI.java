package org.client;

public interface ClientUI {

    public void receiveMessage(String from, String message, boolean isError);
}
