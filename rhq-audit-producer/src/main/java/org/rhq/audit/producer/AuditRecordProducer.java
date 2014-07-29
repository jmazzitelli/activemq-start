package org.rhq.audit.producer;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.AuditRecordProcessor;
import org.rhq.audit.common.ConnectionContext;

/**
 * Produces audit records.
 * 
 * The usage pattern is to create this object with a URL to the broker, then
 * {@link #sendAuditRecord(AuditRecord) send} audit records. When you are done
 * sending, call {@link #close()}.
 */
public class AuditRecordProducer extends AuditRecordProcessor {
    public AuditRecordProducer(String brokerURL) throws JMSException {
        super(brokerURL);
    }

    public void sendAuditRecord(AuditRecord auditRecord) throws JMSException {
        ProducerConnectionContext context = createConnectionContext(auditRecord);
        Message msg = createMessage(context, auditRecord);
        context.getMessageProducer().send(msg);
    }

    /**
     * Creates a new connection context, reusing any existing connection that
     * might have already been created.
     * 
     * @param auditRecord
     * @return the context fully populated
     * @throws JMSException
     */
    protected ProducerConnectionContext createConnectionContext(AuditRecord auditRecord) throws JMSException {
        ProducerConnectionContext context = new ProducerConnectionContext();
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
        createDestination(context, getEndpointFromAuditRecord(auditRecord));
        createProducer(context);
        return context;
    }

    /**
     * Creates a message producer using the context's session and destination.
     * 
     * @param context
     *            the context where the new producer is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null or
     *             the context's destination is null
     */
    protected void createProducer(ProducerConnectionContext context) throws JMSException {
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
        MessageProducer producer = session.createProducer(dest);
        context.setMessageProducer(producer);
    }
    
    /**
     * Creates a text message that is to be produced for the given audit record.
     * 
     * @param context
     *            the context whose session is used to create the message
     * @param auditRecord
     *            the record that will be encapsulated in the created message
     * @return the message that can be produced
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null
     */
    protected Message createMessage(ConnectionContext context, AuditRecord auditRecord) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Message msg = session.createTextMessage(auditRecord.toJSON());
        return msg;
    }
}
