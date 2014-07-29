package org.rhq.audit.consumer;

import javax.jms.MessageConsumer;

import org.rhq.audit.common.ConnectionContext;

public class ConsumerConnectionContext extends ConnectionContext {
    private MessageConsumer consumer;

    public MessageConsumer getMessageConsumer() {
        return consumer;
    }

    public void setMessageConsumer(MessageConsumer consumer) {
        this.consumer = consumer;
    }
}
