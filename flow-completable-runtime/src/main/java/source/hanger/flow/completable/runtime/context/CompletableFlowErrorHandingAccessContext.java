package source.hanger.flow.completable.runtime.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.flow.context.FlowErrorHandingAccessContext;
import source.hanger.flow.core.runtime.FlowExecutionContext;

/**
 * CompletableFuture流程错误处理访问上下文实现
 */
public class CompletableFlowErrorHandingAccessContext extends FlowErrorHandingAccessContext {

    @Serial
    private static final long serialVersionUID = -321773608625335854L;
    private final FlowExecutionContext flowContext;
    private final Throwable exception;

    public CompletableFlowErrorHandingAccessContext(FlowExecutionContext flowContext, Throwable exception) {
        this.flowContext = flowContext;
        this.exception = exception;
    }

    /**
     * 获取异常信息
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * 获取流程执行上下文
     */
    public FlowExecutionContext getFlowContext() {
        return flowContext;
    }

    public java.util.Map<String, java.io.Serializable> getParams() {
        return flowContext.getParams();
    }
} 