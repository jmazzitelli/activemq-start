package org.rhq.audit.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AuditRecord {
    private final String message;
    private final Subsystem subsystem;
    private final long timestamp;
    private final Map<String, String> details;

    public static AuditRecord fromJSON(String json) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, AuditRecord.class);
    }

    public AuditRecord(String message, Subsystem subsystem) {
        this(message, subsystem, null, 0);
    }

    public AuditRecord(String message, Subsystem subsystem, Map<String, String> details) {
        this(message, subsystem, details, 0);
    }

    public AuditRecord(String message, Subsystem subsystem, Map<String, String> details, long timestamp) {
        if (subsystem == null) {
            subsystem = Subsystem.MISCELLANEOUS;
        }
        if (timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }

        this.message = message;
        this.subsystem = subsystem;
        this.timestamp = timestamp;

        // make our own copy of the details data
        if (details != null && !details.isEmpty()) {
            this.details = new HashMap<String, String>(details);
        } else {
            this.details = null;
        }
    }

    public String getMessage() {
        return message;
    }

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public Map<String, String> getDetails() {
        if (details == null) {
            return null;
        }
        return Collections.unmodifiableMap(details);
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return toJSON();
    };

    public String toJSON() {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
