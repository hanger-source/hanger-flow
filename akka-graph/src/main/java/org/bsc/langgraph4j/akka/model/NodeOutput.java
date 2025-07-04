package org.bsc.langgraph4j.akka.model;

import java.io.Serializable;

public class NodeOutput implements Serializable {
    private final State state;
    private final Object output;

    public NodeOutput(State state, Object output) {
        this.state = state;
        this.output = output;
    }

    public State state() {
        return state;
    }

    public Object output() {
        return output;
    }

    public static NodeOutput of(State state) {
        return new NodeOutput(state, null);
    }

    public static NodeOutput of(State state, Object output) {
        return new NodeOutput(state, output);
    }
} 