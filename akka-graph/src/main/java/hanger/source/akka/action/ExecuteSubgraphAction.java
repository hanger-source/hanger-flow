package hanger.source.akka.action;

import akka.stream.javadsl.Source;
import akka.NotUsed;
import hanger.source.akka.model.RunnableConfig;
import hanger.source.akka.shared.FlowData;
import hanger.source.akka.model.State;
import hanger.source.akka.model.NodeOutput;
import hanger.source.akka.graph.CompiledGraphDefinition;
import hanger.source.akka.router.IRouter;

/**
 * 子图节点动作实现。用于标记当前节点为子图入口，实际调度由 NodeExecutorActor/Actor 层完成。
 */
public class ExecuteSubgraphAction implements INodeAction {
    private final String subgraphId;
    private final CompiledGraphDefinition subgraphDefinition;
    private final IRouter<State> router;
    public ExecuteSubgraphAction(String subgraphId, CompiledGraphDefinition subgraphDefinition, IRouter<State> router) {
        this.subgraphId = subgraphId;
        this.subgraphDefinition = subgraphDefinition;
        this.router = router;
    }
    @Override
    public Source<FlowData<NodeOutput>, NotUsed> execute(State inputState, RunnableConfig config) {
        // 这里只做标记，实际子图调度在 NodeExecutorActor/Actor 层
        return Source.single(FlowData.done(NodeOutput.of(inputState)));
    }
    public String subgraphId() { return subgraphId; }
    public CompiledGraphDefinition subgraphDefinition() { return subgraphDefinition; }
    public IRouter<State> router() { return router; }
} 