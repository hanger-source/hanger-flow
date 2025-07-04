package org.bsc.langgraph4j.akka.graph;

import org.bsc.langgraph4j.akka.action.INodeAction;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 编译后的图结构定义，包含节点ID到动作的映射、路由表和入口节点ID。
 * 用于 NodeExecutorActor 等运行时组件高效查找节点逻辑和路由。
 */
public class CompiledGraphDefinition implements Serializable {
    private final Map<String, INodeAction> nodeIdToAction;
    private final Map<String, Map<String, String>> routingTable;
    private final String entryPointId;

    /**
     * 构造函数。
     * @param nodeIdToAction 节点ID到动作的映射
     * @param routingTable 路由表
     * @param entryPointId 入口节点ID
     */
    public CompiledGraphDefinition(Map<String, INodeAction> nodeIdToAction, Map<String, Map<String, String>> routingTable, String entryPointId) {
        this.nodeIdToAction = Collections.unmodifiableMap(new HashMap<>(nodeIdToAction));
        this.routingTable = Collections.unmodifiableMap(new HashMap<>(routingTable));
        this.entryPointId = entryPointId;
    }

    /**
     * 获取节点动作。
     */
    public INodeAction getNodeAction(String nodeId) {
        return nodeIdToAction.get(nodeId);
    }

    /**
     * 获取节点的路由表。
     */
    public Map<String, String> getRoutes(String nodeId) {
        return routingTable.getOrDefault(nodeId, Collections.emptyMap());
    }

    /**
     * 获取入口节点ID。
     */
    public String getEntryPointId() {
        return entryPointId;
    }
} 