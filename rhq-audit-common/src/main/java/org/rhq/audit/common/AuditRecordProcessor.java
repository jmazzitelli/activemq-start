package org.rhq.audit.common;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.rhq.audit.common.Endpoint.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass that provides some functionality to process audit records.
 */
public abstract class AuditRecordProcessor {

    protected final Logger log = LoggerFactory.getLogger(AuditRecordProcessor.class);
    protected final ConnectionFactory connectionFactory;
    private Connection connection;

    public AuditRecordProcessor(String brokerURL) throws JMSException {
        connectionFactory = new ActiveMQConnectionFactory(brokerURL);
        log.debug("{} has been created: {}", this.getClass().getSimpleName(), brokerURL);
    }

    /**
     * This method should be called when this processor is no longer needed.
     * This will free up resources and close any open connection.
     * 
     * @throws JMSException
     */
    public void close() throws JMSException {
        Connection conn = getConnection();
        if (conn != null) {
            conn.close();
        }
        log.debug("{} has been closed", this);
    }

    protected ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    protected Connection getConnection() {
        return connection;
    }

    protected void setConnection(Connection connection) {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (JMSException e) {
                log.error("Cannot close the previous connection; memory might leak.", e);
            }
        }
        this.connection = connection;
    }

    /**
     * Given an audit record, this will return the messaging endpoint that would
     * be appropriate for that record.
     * 
     * @param auditRecord
     *            the record whose endpoint is to be returned
     * @return the endpoint
     */
    protected Endpoint getEndpointFromAuditRecord(AuditRecord auditRecord) {
        if (auditRecord == null) {
            throw new NullPointerException("auditRecord is null");
        }
        return getEndpointFromSubsystem(auditRecord.getSubsystem());
    }

    /**
     * Given a subsystem, this will return the messaging endpoint that would be
     * appropriate for that record. Messages for that subsystem will be sent and
     * received from the returned endpoint.
     * 
     * @param subsystem
     *            the subsystem whose endpoint is to be returned
     * @return the endpoint
     */
    protected Endpoint getEndpointFromSubsystem(Subsystem subsystem) {
        if (subsystem == null) {
            throw new NullPointerException("subsystem is null");
        }
        return new Endpoint(Type.QUEUE, subsystem.getName());
    }

    /**
     * Creates a connection using this object's connection factory.
     * 
     * @param context
     *            the context where the new connection is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null
     */
    protected void createConnection(ConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        ConnectionFactory factory = getConnectionFactory();
        Connection conn = factory.createConnection();
        context.setConnection(conn);
    }

    /**
     * Creates a default session using the context's connection. This
     * implementation creates a non-transacted, auto-acknowledged session.
     * Subclasses are free to override this behavior.
     * 
     * @param context
     *            the context where the new session is stored
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's connection is null
     */
    protected void createSession(ConnectionContext context) throws JMSException {
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Connection conn = context.getConnection();
        if (conn == null) {
            throw new NullPointerException("The context had a null connection");
        }
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        context.setSession(session);
    }

    /**
     * Creates a destination using the context's session. The destination
     * correlates to the given named queue or topic.
     * 
     * @param context
     *            the context where the new destination is stored
     * @param endpoint
     *            identifies the queue or topic
     * @throws JMSException
     * @throws NullPointerException
     *             if the context is null or the context's session is null or
     *             endpoint is null
     */
    protected void createDestination(ConnectionContext context, Endpoint endpoint) throws JMSException {
        if (endpoint == null) {
            throw new NullPointerException("Endpoint is null");
        }
        if (context == null) {
            throw new NullPointerException("The context is null");
        }
        Session session = context.getSession();
        if (session == null) {
            throw new NullPointerException("The context had a null session");
        }
        Destination dest;
        if (endpoint.getType() == Type.QUEUE) {
            dest = session.createQueue(endpoint.getName());
        } else {
            dest = session.createTopic(endpoint.getName());
        }
        context.setDestination(dest);
    }

}