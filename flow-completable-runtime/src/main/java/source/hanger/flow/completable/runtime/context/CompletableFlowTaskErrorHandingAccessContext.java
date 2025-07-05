package source.hanger.flow.completable.runtime.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.task.context.FlowTaskErrorHandingAccessContext;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture任务错误处理访问上下文实现
 */
public class CompletableFlowTaskErrorHandingAccessContext extends FlowTaskErrorHandingAccessContext {

    @Serial
    private static final long serialVersionUID = 6309989433208908325L;
    private final FlowExecutionContext flowContext;
    private final String stepName;
    private final Throwable exception;

    public CompletableFlowTaskErrorHandingAccessContext(FlowExecutionContext flowContext, String stepName,
        Throwable exception) {
        this.flowContext = flowContext;
        this.stepName = stepName;
        this.exception = exception;
    }

    /**
     * 获取流程执行上下文
     */
    public FlowExecutionContext getFlowContext() {
        return flowContext;
    }

    /**
     * 获取步骤名称
     */
    public String getStepName() {
        return stepName;
    }

    /**
     * 获取异常信息
     */
    public Throwable getException() {
        return exception;
    }

    public java.util.Map<String, java.io.Serializable> getParams() {
        return flowContext.getParams();
    }
} 