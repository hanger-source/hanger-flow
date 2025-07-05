package source.hanger.flow.contract.runtime.task.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskRunAccessContext;

/**
 * 任务执行访问接口
 * 继承通用上下文，提供任务执行过程中的上下文访问能力
 * 
 * @author fuhangbo.hanger.uhfun
 **/
// 继承通用上下文
public interface FlowTaskRunAccess extends FlowRuntimeAccess<FlowTaskRunAccessContext> {
    /**
     * 获取任务执行上下文
     * 
     * @return 任务执行上下文
     */
    FlowTaskRunAccessContext getContext();
}