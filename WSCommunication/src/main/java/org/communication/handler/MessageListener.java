package org.communication.handler;

import org.communication.Message;

public interface MessageListener {

    void onMessageReceived(Message message);
}