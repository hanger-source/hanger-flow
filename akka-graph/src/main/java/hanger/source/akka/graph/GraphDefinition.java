package hanger.source.akka.graph;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 原始图结构定义，包含节点、边、入口节点、子图等。
 * 用于描述主图和子图的原始结构，便于统一编译和复用。
 */
public record GraphDefinition(List<GraphCompiler.GraphNodeDef> nodes, List<GraphCompiler.GraphEdgeDef> edges,
                              String entryPointId, Map<String, SubgraphDef> subgraphs) implements Serializable {
    @Serial
    private static final long serialVersionUID = 8321843395989111965L;

    /**
     * 构造函数。
     *
     * @param nodes        节点定义列表
     * @param edges        边定义列表
     * @param entryPointId 入口节点ID
     * @param subgraphs    子图定义Map
     */
    public GraphDefinition {
    }
} 