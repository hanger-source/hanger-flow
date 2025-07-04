package org.bsc.langgraph4j.akka.model;

/**
 * NodeStatus 表示节点的执行状态（如未开始、运行中、已完成、失败等）。
 * 典型用法：用于跟踪和展示节点生命周期。
 */
public enum NodeStatus {
    /** 未开始 */
    NOT_STARTED,
    /** 运行中 */
    RUNNING,
    /** 已完成 */
    COMPLETED,
    /** 失败 */
    FAILED
} 