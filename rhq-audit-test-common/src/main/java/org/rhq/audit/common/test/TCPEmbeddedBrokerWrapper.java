package org.rhq.audit.common.test;

import java.net.ServerSocket;

import org.rhq.audit.broker.EmbeddedBroker;

/**
 * Used to start a simple test broker that accepts messages from remote TCP
 * clients.
 */
public class TCPEmbeddedBrokerWrapper extends AbstractEmbeddedBrokerWrapper {

    private final int bindPort;

    public TCPEmbeddedBrokerWrapper() throws Exception {
        bindPort = findFreePort();
        setBroker(new EmbeddedBroker(new String[] { "--config=simple-activemq.xml", "-Dtcp-testbroker.bind.port=" + bindPort }));
    }

    @Override
    public String getBrokerURL() {
        return "tcp://localhost:" + bindPort;
    }

    protected int findFreePort() throws Exception {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(0);
            return ss.getLocalPort();
        } finally {
            if (ss != null) {
                ss.close();
            }
        }
    }
}
