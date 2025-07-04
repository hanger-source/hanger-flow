package org.bsc.langgraph4j.akka.graph;

import org.bsc.langgraph4j.akka.action.INodeAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图编译器，将原始节点和边定义编译为 CompiledGraphDefinition。
 * 支持主图和子图的统一编译。
 */
public class GraphCompiler {
    /**
     * 编译节点和边为 CompiledGraphDefinition。
     * @param nodes 节点定义列表
     * @param edges 边定义列表
     * @param entryPointId 入口节点ID
     * @return 编译后的图结构
     */
    public static CompiledGraphDefinition compile(List<GraphNodeDef> nodes, List<GraphEdgeDef> edges, String entryPointId) {
        Map<String, INodeAction> nodeIdToAction = new HashMap<>();
        for (GraphNodeDef node : nodes) {
            nodeIdToAction.put(node.id(), node.action());
        }
        Map<String, Map<String, String>> routingTable = new HashMap<>();
        for (GraphEdgeDef edge : edges) {
            routingTable.computeIfAbsent(edge.from(), k -> new HashMap<>()).put(edge.condition(), edge.to());
        }
        return new CompiledGraphDefinition(nodeIdToAction, routingTable, entryPointId);
    }

    /**
     * 节点定义。
     */
    public static class GraphNodeDef {
        private final String id;
        private final INodeAction action;
        public GraphNodeDef(String id, INodeAction action) {
            this.id = id;
            this.action = action;
        }
        public String id() { return id; }
        public INodeAction action() { return action; }
    }

    /**
     * 边定义。
     */
    public static class GraphEdgeDef {
        private final String from;
        private final String condition;
        private final String to;
        public GraphEdgeDef(String from, String condition, String to) {
            this.from = from;
            this.condition = condition;
            this.to = to;
        }
        public String from() { return from; }
        public String condition() { return condition; }
        public String to() { return to; }
    }
} 