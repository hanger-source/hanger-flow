package source.hanger.akkagraph.graph;

import source.hanger.akkagraph.action.INodeAction;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * 编译后的图结构定义，包含节点ID到动作的映射、路由表和入口节点ID。
 * 用于 NodeExecutorActor 等运行时组件高效查找节点逻辑和路由。
 */
public class CompiledGraphDefinition implements Serializable {
    @Serial
    private static final long serialVersionUID = -237030244384415590L;
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
        this.nodeIdToAction = Map.copyOf(nodeIdToAction);
        this.routingTable = Map.copyOf(routingTable);
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

    /**
     * 获取所有已注册节点ID。
     */
    public Set<String> getAllNodeIds() {
        return nodeIdToAction.keySet();
    }
} 