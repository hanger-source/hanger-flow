package source.hanger.flow.contract.runtime.task.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowEnterHandingAccessContext;
import source.hanger.flow.contract.runtime.task.context.FlowTaskEnterHandingAccessContext;

/**
 * 任务进入处理访问接口
 * 继承通用上下文，提供任务进入处理时的上下文访问能力
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowTaskEnterHandingAccess extends FlowRuntimeAccess<FlowTaskEnterHandingAccessContext> {
    /**
     * 获取任务进入处理上下文
     * 
     * @return 任务进入处理上下文
     */
    FlowTaskEnterHandingAccessContext getContext();
}