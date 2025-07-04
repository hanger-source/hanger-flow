package org.bsc.langgraph4j.akka.action;

import akka.stream.javadsl.Source;
import akka.NotUsed;
import org.bsc.langgraph4j.akka.model.State;
import org.bsc.langgraph4j.akka.model.NodeOutput;
import org.bsc.langgraph4j.akka.graph.CompiledGraphDefinition;
import org.bsc.langgraph4j.akka.model.RunnableConfig;
import org.bsc.langgraph4j.akka.shared.FlowData;

/**
 * 子图节点动作实现。用于标记当前节点为子图入口，实际调度由 NodeExecutorActor/Actor 层完成。
 */
public class ExecuteSubgraphAction implements INodeAction {
    private final String subgraphId;
    private final CompiledGraphDefinition subgraphDefinition;
    public ExecuteSubgraphAction(String subgraphId, CompiledGraphDefinition subgraphDefinition) {
        this.subgraphId = subgraphId;
        this.subgraphDefinition = subgraphDefinition;
    }
    @Override
    public Source<FlowData<NodeOutput>, NotUsed> execute(State inputState, RunnableConfig config) {
        // 这里只做标记，实际子图调度在 NodeExecutorActor/Actor 层
        return Source.single(FlowData.done(NodeOutput.of(inputState)));
    }
    public String subgraphId() { return subgraphId; }
    public CompiledGraphDefinition subgraphDefinition() { return subgraphDefinition; }
} 