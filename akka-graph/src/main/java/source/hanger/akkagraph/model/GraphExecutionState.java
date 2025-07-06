package source.hanger.akkagraph.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * GraphExecutionState 记录当前图执行点、状态、并行分支等信息。
 * 用于持久化和恢复。
 */
public class GraphExecutionState implements Serializable {
    @Serial
    private static final long serialVersionUID = -1577583936773770823L;
    private final String currentNodeId;
    private final State currentState;
    private final Map<String, NodeOutput> parallelOutputs;
    private final Optional<Object> replyTo;
    private final Optional<Object> managerRef;

    /**
     * 构造函数。
     * @param currentNodeId 当前节点ID
     * @param currentState 当前状态
     * @param parallelOutputs 并行分支输出
     * @param replyTo 外部回复目标
     * @param managerRef 管理者Actor引用
     */
    public GraphExecutionState(String currentNodeId, State currentState, Map<String, NodeOutput> parallelOutputs, Object replyTo, Object managerRef) {
        this.currentNodeId = currentNodeId;
        this.currentState = currentState;
        this.parallelOutputs = parallelOutputs == null ? new HashMap<>() : new HashMap<>(parallelOutputs);
        this.replyTo = Optional.ofNullable(replyTo);
        this.managerRef = Optional.ofNullable(managerRef);
    }

    public String currentNodeId() { return currentNodeId; }
    public State currentState() { return currentState; }
    public Map<String, NodeOutput> parallelOutputs() { return parallelOutputs; }
    public Optional<Object> replyTo() { return replyTo; }
    public Optional<Object> managerRef() { return managerRef; }

    /**
     * 返回更新节点和状态的新实例。
     */
    public GraphExecutionState withNode(String nodeId, State state) {
        return new GraphExecutionState(nodeId, state, parallelOutputs, replyTo.orElse(null), managerRef.orElse(null));
    }
    /**
     * 返回包含新并行分支输出的新实例。
     */
    public GraphExecutionState withParallelOutput(String branchId, NodeOutput output) {
        Map<String, NodeOutput> newMap = new HashMap<>(parallelOutputs);
        newMap.put(branchId, output);
        return new GraphExecutionState(currentNodeId, currentState, newMap, replyTo.orElse(null), managerRef.orElse(null));
    }
    public GraphExecutionState withReplyTo(Object replyTo) {
        return new GraphExecutionState(currentNodeId, currentState, parallelOutputs, replyTo, managerRef.orElse(null));
    }
    public GraphExecutionState withManagerRef(Object managerRef) {
        return new GraphExecutionState(currentNodeId, currentState, parallelOutputs, replyTo.orElse(null), managerRef);
    }
} 