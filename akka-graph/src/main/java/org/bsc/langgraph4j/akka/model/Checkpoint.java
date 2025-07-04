package org.bsc.langgraph4j.akka.model;

import java.io.Serializable;

/**
 * Checkpoint 用于保存图执行过程中的快照，支持持久化与恢复。
 * 典型用法：GraphManagerActor/子图Actor 定期保存 Checkpoint，实现故障恢复。
 */
public class Checkpoint implements Serializable {
    private final String graphId;
    private final GraphExecutionState executionState;

    /**
     * 构造函数。
     * @param graphId 图唯一标识
     * @param executionState 当前执行状态
     */
    public Checkpoint(String graphId, GraphExecutionState executionState) {
        this.graphId = graphId;
        this.executionState = executionState;
    }
    /**
     * 获取图ID。
     */
    public String graphId() {
        return graphId;
    }
    /**
     * 获取执行状态。
     */
    public GraphExecutionState executionState() {
        return executionState;
    }
} 