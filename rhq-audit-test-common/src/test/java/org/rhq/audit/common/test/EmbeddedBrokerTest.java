package org.rhq.audit.common.test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.rhq.audit.common.Endpoint;
import org.rhq.audit.common.Endpoint.Type;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test class shows usages of the different Embedded Broker Wrapper objects
 * as well as the convenience connections for both consumer and producer.
 */
@Test
public class EmbeddedBrokerTest {
    public void testInternalVMBrokerQueue() throws Exception {
        internalTestBroker(new VMEmbeddedBrokerWrapper(), new Endpoint(Type.QUEUE, "testq"));
    }

    public void testInternalVMBrokerTopic() throws Exception {
        internalTestBroker(new VMEmbeddedBrokerWrapper(), new Endpoint(Type.TOPIC, "testtopic"));
    }

    public void testTCPBrokerQueue() throws Exception {
        internalTestBroker(new TCPEmbeddedBrokerWrapper(), new Endpoint(Type.QUEUE, "testq"));
    }

    public void testTCPBrokerTopic() throws Exception {
        internalTestBroker(new TCPEmbeddedBrokerWrapper(), new Endpoint(Type.TOPIC, "testtopic"));
    }

    private void internalTestBroker(AbstractEmbeddedBrokerWrapper broker, Endpoint endpoint) throws Exception {
        broker.start();

        try {
            String brokerURL = broker.getBrokerURL();

            // test that messages can flow to the given broker
            CountDownLatch latch = new CountDownLatch(1);
            String testMessage = "Hello World!";
            ArrayList<String> receivedMessages = new ArrayList<String>();
            ArrayList<String> errors = new ArrayList<String>();

            // start the consumer
            StoreAndLatchMessageListener messageListener = new StoreAndLatchMessageListener(latch, receivedMessages, errors);
            ConsumerConnection consumerConnection = new ConsumerConnection(brokerURL, endpoint, messageListener);

            // start the producer
            ProducerConnection producerConnection = new ProducerConnection(brokerURL, endpoint);
            producerConnection.sendMessage(testMessage);

            // wait for the message to flow
            boolean gotMessage = latch.await(5, TimeUnit.SECONDS);
            if (!gotMessage) {
                errors.add("Timed out waiting for message - it never showed up");
            }

            // close everything
            producerConnection.close();
            consumerConnection.close();

            // make sure the message flowed properly
            Assert.assertTrue(errors.isEmpty(), "Failed to send message propertly: " + errors);
            Assert.assertEquals(receivedMessages.size(), 1, "Didn't receive message: " + receivedMessages);
            Assert.assertEquals(receivedMessages.get(0), testMessage);
        } finally {
            broker.stop();
        }
    }
}
