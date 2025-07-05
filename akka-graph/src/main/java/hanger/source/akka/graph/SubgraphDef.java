package hanger.source.akka.graph;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 子图结构定义，包含子图ID、节点、边和入口节点。
 * 用于主图引用和统一编译。
 */
public record SubgraphDef(String id, List<GraphCompiler.GraphNodeDef> nodes, List<GraphCompiler.GraphEdgeDef> edges,
                          String entryPointId) implements Serializable {
    @Serial
    private static final long serialVersionUID = -3151034731027332576L;

    /**
     * 构造函数。
     *
     * @param id           子图ID
     * @param nodes        子图节点定义
     * @param edges        子图边定义
     * @param entryPointId 子图入口节点ID
     */
    public SubgraphDef {
    }
} 