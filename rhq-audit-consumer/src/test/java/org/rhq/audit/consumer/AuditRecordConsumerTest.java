package org.rhq.audit.consumer;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.audit.common.test.VMEmbeddedBrokerWrapper;
import org.rhq.audit.producer.AuditRecordProducer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AuditRecordConsumerTest {

    private AuditRecordProducer producer;
    private AuditRecordConsumer consumer;

    @BeforeMethod
    public void setupProducerAndConsumer() throws Exception {
        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProducer(brokerURL);
        consumer = new AuditRecordConsumer(brokerURL);
    }

    @AfterMethod
    public void teardownProducerAndConsumer() throws Exception {
        if (producer != null) {
            producer.close();
        }
        if (consumer != null) {
            consumer.close();
        }
    }

    public void testMessageSend() throws Exception {
        final int numberOfTestRecords = 5;
        final CountDownLatch latch = new CountDownLatch(numberOfTestRecords);
        final ArrayList<AuditRecord> records = new ArrayList<AuditRecord>();
        final StoreAndLatchAuditRecordListener listener = new StoreAndLatchAuditRecordListener(latch, records, null);

        consumer.listen(Subsystem.MISCELLANEOUS, listener);

        // send some audit records
        for (int i = 0; i < numberOfTestRecords; i++) {
            AuditRecord auditRecord = new AuditRecord("test message#" + i, Subsystem.MISCELLANEOUS);
            producer.sendAuditRecord(auditRecord);
        }
        latch.await(5, TimeUnit.SECONDS);

        // make sure the audit records flowed properly
        Assert.assertEquals(numberOfTestRecords, records.size());
        for (int i = 0; i < numberOfTestRecords; i++) {
            Assert.assertEquals(Subsystem.MISCELLANEOUS, records.get(i).getSubsystem());
            Assert.assertEquals("test message#" + i, records.get(i).getMessage());
            Assert.assertNull(records.get(i).getDetails());
            Assert.assertTrue(records.get(i).getTimestamp() > 0L);
        }
    }
}
