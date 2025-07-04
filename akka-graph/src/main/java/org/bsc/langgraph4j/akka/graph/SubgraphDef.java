package org.bsc.langgraph4j.akka.graph;

import java.io.Serializable;
import java.util.List;

/**
 * 子图结构定义，包含子图ID、节点、边和入口节点。
 * 用于主图引用和统一编译。
 */
public class SubgraphDef implements Serializable {
    private final String id;
    private final List<GraphCompiler.GraphNodeDef> nodes;
    private final List<GraphCompiler.GraphEdgeDef> edges;
    private final String entryPointId;

    /**
     * 构造函数。
     * @param id 子图ID
     * @param nodes 子图节点定义
     * @param edges 子图边定义
     * @param entryPointId 子图入口节点ID
     */
    public SubgraphDef(String id, List<GraphCompiler.GraphNodeDef> nodes, List<GraphCompiler.GraphEdgeDef> edges, String entryPointId) {
        this.id = id;
        this.nodes = nodes;
        this.edges = edges;
        this.entryPointId = entryPointId;
    }
    public String id() { return id; }
    public List<GraphCompiler.GraphNodeDef> nodes() { return nodes; }
    public List<GraphCompiler.GraphEdgeDef> edges() { return edges; }
    public String entryPointId() { return entryPointId; }
} 