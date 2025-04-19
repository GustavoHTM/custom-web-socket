package org.server.handlers;

import org.communication.Message;
import org.server.Client;

public interface ServerMessageListener {

    void onMessageReceived(Client client, Message message);

}