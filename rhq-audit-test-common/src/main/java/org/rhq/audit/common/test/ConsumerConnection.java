package org.rhq.audit.common.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.rhq.audit.common.Endpoint;
import org.rhq.audit.common.Endpoint.Type;

/**
 * Convenience class tests can use to create a consumer of either topic or queue
 * messages from a broker.
 * 
 * The constructor creates the connection and attaches the listener after which
 * the listener can start consuming messages as they are produced.
 */
public class ConsumerConnection {
    private Connection connection;

    public ConsumerConnection(String brokerURL, Endpoint endpoint, MessageListener messageListener) throws JMSException {
        createConnection(brokerURL, endpoint, messageListener);
    }

    protected void createConnection(String brokerURL, Endpoint endpoint, MessageListener messageListener) throws JMSException {
        ConnectionFactory connFactory = new ActiveMQConnectionFactory(brokerURL);
        Connection conn = connFactory.createConnection();
        conn.start();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest;
        if (endpoint.getType() == Type.QUEUE) {
            dest = session.createQueue(endpoint.getName());
        } else {
            dest = session.createTopic(endpoint.getName());
        }
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(messageListener);
        this.connection = conn;
    }

    public void close() throws JMSException {
        connection.close();
    }
}
