package source.hanger.flow.contract.runtime.flow.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowErrorHandingAccessContext;

/**
 * 流程错误处理访问接口
 * 继承通用上下文，提供流程错误处理时的上下文访问能力
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowErrorHandlingAccess extends FlowRuntimeAccess<FlowErrorHandingAccessContext> {
    /**
     * 获取异常信息
     * 
     * @return 导致流程错误的异常对象
     */
    Throwable getException();

    /**
     * 获取流程错误处理上下文
     * 
     * @return 流程错误处理上下文
     */
    @Override
    FlowErrorHandingAccessContext getContext();
}