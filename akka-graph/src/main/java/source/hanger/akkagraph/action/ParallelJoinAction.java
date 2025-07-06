package source.hanger.akkagraph.action;

import akka.stream.javadsl.Source;
import akka.NotUsed;
import source.hanger.akkagraph.model.State;
import source.hanger.akkagraph.model.NodeOutput;
import source.hanger.akkagraph.model.RunnableConfig;
import source.hanger.akkagraph.shared.FlowData;

/**
 * 并行聚合节点动作实现。用于聚合所有并行分支的结果。
 */
public class ParallelJoinAction implements INodeAction {
    /**
     * 执行聚合动作，默认将状态加上 parallel_joined 标记。
     */
    @Override
    public Source<FlowData<NodeOutput>, NotUsed> execute(State inputState, RunnableConfig config) {
        return Source.single(FlowData.done(NodeOutput.of(inputState.with("parallel_joined", true))));
    }
} 