package org.rhq.audit.producer;

import org.rhq.audit.common.AuditRecord;
import org.rhq.audit.common.Subsystem;
import org.rhq.audit.common.test.VMEmbeddedBrokerWrapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AuditRecordProducerTest {

    private AuditRecordProducer producer;

    @BeforeMethod
    public void setupProducer() throws Exception {
        VMEmbeddedBrokerWrapper broker = new VMEmbeddedBrokerWrapper();
        broker.start();
        String brokerURL = broker.getBrokerURL();
        producer = new AuditRecordProducer(brokerURL);
    }

    @AfterMethod
    public void teardownProducer() throws Exception {
        if (producer != null) {
            producer.close();
        }
    }

    public void testMessageSend() throws Exception {
        AuditRecord auditRecord;
        auditRecord = new AuditRecord("test message", Subsystem.MISCELLANEOUS);
        producer.sendAuditRecord(auditRecord);
    }
}
