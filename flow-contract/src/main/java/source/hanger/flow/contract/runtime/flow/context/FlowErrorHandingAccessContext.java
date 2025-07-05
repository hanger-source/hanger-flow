package source.hanger.flow.contract.runtime.flow.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccessContext;

/**
 * 流程错误处理访问上下文
 * 继承自FlowRuntimeAccessContext，专门用于流程错误处理时的上下文数据存储
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class FlowErrorHandingAccessContext extends FlowRuntimeAccessContext {
    @Serial
    private static final long serialVersionUID = -6099466616844523546L;
    
    /**
     * 获取异常信息
     * 
     * @return 导致流程错误的异常对象
     */
    public abstract Throwable getException();
}
