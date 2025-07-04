package org.bsc.langgraph4j.akka.graph;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 原始图结构定义，包含节点、边、入口节点、子图等。
 * 用于描述主图和子图的原始结构，便于统一编译和复用。
 */
public class GraphDefinition implements Serializable {
    private final List<GraphCompiler.GraphNodeDef> nodes;
    private final List<GraphCompiler.GraphEdgeDef> edges;
    private final String entryPointId;
    private final Map<String, SubgraphDef> subgraphs;

    /**
     * 构造函数。
     * @param nodes 节点定义列表
     * @param edges 边定义列表
     * @param entryPointId 入口节点ID
     * @param subgraphs 子图定义Map
     */
    public GraphDefinition(List<GraphCompiler.GraphNodeDef> nodes, List<GraphCompiler.GraphEdgeDef> edges, String entryPointId, Map<String, SubgraphDef> subgraphs) {
        this.nodes = nodes;
        this.edges = edges;
        this.entryPointId = entryPointId;
        this.subgraphs = subgraphs;
    }
    public List<GraphCompiler.GraphNodeDef> nodes() { return nodes; }
    public List<GraphCompiler.GraphEdgeDef> edges() { return edges; }
    public String entryPointId() { return entryPointId; }
    public Map<String, SubgraphDef> subgraphs() { return subgraphs; }
} 