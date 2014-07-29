package org.rhq.audit.common;

/**
 * POJO that indicates the type of endpoint to use (queue or topic) and that
 * queue or topic's name.
 */
public class Endpoint {
    public enum Type {
        QUEUE, TOPIC
    }

    private final Type type;
    private final String name;

    public Endpoint(Type type, String name) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{" + type.name() + "}" + name;
    }

}
