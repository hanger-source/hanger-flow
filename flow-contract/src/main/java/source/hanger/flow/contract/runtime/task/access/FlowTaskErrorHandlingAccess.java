package source.hanger.flow.contract.runtime.task.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskErrorHandingAccessContext;

/**
 * 任务错误处理访问接口
 * 继承通用上下文，提供任务错误处理时的上下文访问能力
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowTaskErrorHandlingAccess extends FlowRuntimeAccess<FlowTaskErrorHandingAccessContext> {
    /**
     * 获取异常信息
     * 
     * @return 导致任务错误的异常对象
     */
    Throwable getException();

    /**
     * 获取任务错误处理上下文
     * 
     * @return 任务错误处理上下文
     */
    @Override
    FlowTaskErrorHandingAccessContext getContext();
}