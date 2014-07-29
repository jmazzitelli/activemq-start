package org.rhq.audit.common;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Session;

/**
 * Contains objects related to particular connection.
 */
public class ConnectionContext {
    private Connection connection;
    private Session session;
    private Destination destination;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
