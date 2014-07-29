package org.rhq.audit.consumer;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.audit.common.Subsystem;

/**
 * Consumes audit records.
 * 
 * The usage pattern is to create this object with a URL to the broker, then
 * {@link #listen(Subsystem, AuditRecordListener) listen} for audit records.
 * When you are done listening, call {@link #close()}.
 */
public class AuditRecordConsumer extends AuditRecordProcessor {
    public AuditRecordConsumer(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    /**
     * Listens for audit records for the given subsystem.
     * 
     * @param subsystem
     *            identifies the types of audit records that are being listened
     *            for
     * @param listener
     *            the listener that processes the incoming audit records
     * @throws JMSException
     */
    public void listen(Subsystem subsystem, AuditRecordListener listener) throws JMSException {
        ConsumerConnectionContext context = createConnectionContext(subsystem);
        MessageConsumer consumer = context.getMessageConsumer();
        consumer.setMessageListener(listener);
    }

    /**
     * Creates a new connection context, reusing any existing connection that
     * might have already been created.
     * 
     * @param subsystem
     * @return the context fully populated
     * @throws JMSException
     */
    protected ConsumerConnectionContext createConnectionContext(Subsystem subsystem) throws JMSException {
        ConsumerConnectionContext context = new ConsumerConnectionContext();
        // reuse our connection - creating one only if there is no existing
        // connection yet
        Connection conn = getConnection();
        if (conn != null) {
            context.setConnection(conn);
        } else {
            createConnection(context);
            conn = context.getConnection();
            setConnection(conn);
            conn.start(); // start it immediately so the caller doesn't have to
        }

        createSession(context);
        createDestination(context, getEndpointFromSubsystem(subsystem));
        createConsumer(context);
        return context;
    }

    /**
     * Creates a message consumer using the context's session and destination.
     * 
     * @param context
     *            the context where the new consumer is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null or
     *             the context's destination is null
     */
    protected void createConsumer(ConsumerConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Destination dest = context.getDestination();
        if (dest == null) {
            throw new NullPointerException("The context had a null destination");
        }
        MessageConsumer consumer = session.createConsumer(dest);
        context.setMessageConsumer(consumer);
    }
}
