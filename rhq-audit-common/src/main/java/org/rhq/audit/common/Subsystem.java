package org.rhq.audit.common;

/**
 * Identifies the name of an audited subsystem.
 */
public class Subsystem {
    // a generic catch-all subsystem
    public static final Subsystem MISCELLANEOUS = new Subsystem("MISC");

    private final String name;

    public Subsystem(String name) {
        if (name == null || name.length() == 0) {
            throw new NullPointerException("subsystem name cannot be null or empty");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Subsystem)) {
            return false;
        }
        Subsystem other = (Subsystem) obj;
        return name.equals(other.name);
    }
}
