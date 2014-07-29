package org.rhq.audit.common.test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Creates a simple test listener. This listener will log messages it receives
 * and errors it encounters in lists given to it via its constructor.
 * 
 * This listener will notify when it gets a message by counting down a latch.
 */
public class StoreAndLatchMessageListener implements MessageListener {

    private final CountDownLatch latch;
    private final ArrayList<String> messages;
    private final ArrayList<String> errors;

    public StoreAndLatchMessageListener(CountDownLatch latch, ArrayList<String> messages, ArrayList<String> errors) {
        this.latch = latch;
        this.messages = messages;
        this.errors = errors;
    }

    public void onMessage(Message message) {
        try {
            String receivedMessage = ((TextMessage) message).getText();
            if (messages != null) {
                messages.add(receivedMessage);
            }
        } catch (Exception e) {
            if (errors != null) {
                errors.add(e.toString());
            } else {
                e.printStackTrace();
            }
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}