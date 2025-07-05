package hanger.source.akka.action;

import akka.stream.javadsl.Source;
import akka.NotUsed;
import hanger.source.akka.model.NodeOutput;
import hanger.source.akka.model.State;
import hanger.source.akka.model.RunnableConfig;
import hanger.source.akka.shared.FlowData;

/**
 * INodeAction 定义节点动作的流式执行接口。
 * 所有节点实现均应返回 Source<FlowData<NodeOutput>, NotUsed>，
 * 支持流式 LLM 输出、进度报告、最终结果和异常。
 */
public interface INodeAction {
    /**
     * 执行节点动作，返回流式输出。
     * @param inputState 输入状态
     * @param config 运行时配置
     * @return Akka Stream Source，发出 FlowData<NodeOutput> 序列
     */
    Source<FlowData<NodeOutput>, NotUsed> execute(State inputState, RunnableConfig config);
}   