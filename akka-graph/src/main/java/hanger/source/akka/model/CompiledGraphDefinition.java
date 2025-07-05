package hanger.source.akka.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import hanger.source.akka.graph.GraphDefinition;

/**
 * CompiledGraphDefinition 封装编译后的主图或子图结构，包含节点定义、入口、终止节点等。
 * 用于 Actor 执行时的静态图结构描述。
 * 典型用法：GraphCompiler.compile(...) 生成后传递给 GraphManagerActor 或 SubgraphActor。
 */
public record CompiledGraphDefinition(String graphId, String entryNodeId, Map<String, Object> nodeDefs,
                                      String endNodeId, GraphDefinition rawDefinition) implements Serializable {
    @Serial
    private static final long serialVersionUID = 4875100161630986578L;

    /**
     * 构造函数。
     *
     * @param graphId       图唯一标识
     * @param entryNodeId   入口节点ID
     * @param nodeDefs      节点定义Map
     * @param endNodeId     终止节点ID
     * @param rawDefinition 原始图定义
     */
    public CompiledGraphDefinition {
    }
} 