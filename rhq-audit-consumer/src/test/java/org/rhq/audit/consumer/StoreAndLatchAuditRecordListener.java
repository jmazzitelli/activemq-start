package org.rhq.audit.consumer;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.rhq.audit.common.AuditRecord;

public class StoreAndLatchAuditRecordListener extends AuditRecordListener {

    private final CountDownLatch latch;
    private final ArrayList<AuditRecord> auditRecords;
    private final ArrayList<String> errors;

    public StoreAndLatchAuditRecordListener(CountDownLatch latch, ArrayList<AuditRecord> auditRecords, ArrayList<String> errors) {
        this.latch = latch;
        this.auditRecords = auditRecords;
        this.errors = errors;
    }

    @Override
    protected void onAuditRecord(AuditRecord auditRecord) {
        try {
            if (auditRecords != null) {
                auditRecords.add(auditRecord);
            }
        } catch (Exception e) {
            if (errors != null) {
                errors.add(e.toString());
            } else {
                e.printStackTrace();
            }
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }

}
