package org.rhq.audit.common.test;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.rhq.audit.common.Endpoint;
import org.rhq.audit.common.Endpoint.Type;

/**
 * Convenience class tests can use to create a producer of either topic or queue
 * messages.
 * 
 * The constructor creates the connection after which you just call sendMessage
 * to produce a message.
 */
public class ProducerConnection {
    private Connection connection;
    private MessageProducer producer;
    private Session session;

    public ProducerConnection(String brokerURL, Endpoint endpoint) throws JMSException {
        createConnection(brokerURL, endpoint);
    }

    protected void createConnection(String brokerURL, Endpoint endpoint) throws JMSException {
        ConnectionFactory connFactory = new ActiveMQConnectionFactory(brokerURL);
        Connection conn = connFactory.createConnection();
        conn.start();
        this.session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest;
        if (endpoint.getType() == Type.QUEUE) {
            dest = session.createQueue(endpoint.getName());
        } else {
            dest = session.createTopic(endpoint.getName());
        }

        this.connection = conn;
        this.producer = session.createProducer(dest);
    }

    public void sendMessage(String msg) throws JMSException {
        Message producerMessage = session.createTextMessage(msg);
        producer.send(producerMessage);

    }

    public void close() throws JMSException {
        connection.close();
    }
}
