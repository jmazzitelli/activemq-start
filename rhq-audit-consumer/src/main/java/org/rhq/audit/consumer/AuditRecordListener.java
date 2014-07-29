package org.rhq.audit.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.rhq.audit.common.AuditRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message listener that expects to receive a JSON-encoded AuditRecord.
 * Implementors need only implement the method that takes an AuditRecord; the
 * JSON decoding is handled for you.
 */
public abstract class AuditRecordListener implements MessageListener {
    protected final Logger log = LoggerFactory.getLogger(AuditRecordListener.class);

    @Override
    public void onMessage(Message message) {
        AuditRecord auditRecord;

        try {
            String receivedMessage = ((TextMessage) message).getText();
            auditRecord = AuditRecord.fromJSON(receivedMessage);
        } catch (JMSException e) {
            log.error("A message was received that was not a valid text message");
            return;
        } catch (Exception e) {
            log.error("A message was received that was not a valid JSON-encoded AuditRecord");
            return;
        }

        onAuditRecord(auditRecord);
    }

    protected abstract void onAuditRecord(AuditRecord auditRecord);

}
