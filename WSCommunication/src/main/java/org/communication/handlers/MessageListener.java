package org.communication.handlers;

import org.communication.Message;

public interface MessageListener {

    void onMessageReceived(Message message);

}