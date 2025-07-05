package hanger.source.akka.model;

import java.io.Serial;
import java.io.Serializable;

public record NodeOutput(State state, Object output) implements Serializable {
    @Serial
    private static final long serialVersionUID = -5868133897629526010L;

    public static NodeOutput of(State state) {
        return new NodeOutput(state, null);
    }

    public static NodeOutput of(State state, Object output) {
        return new NodeOutput(state, output);
    }
} 