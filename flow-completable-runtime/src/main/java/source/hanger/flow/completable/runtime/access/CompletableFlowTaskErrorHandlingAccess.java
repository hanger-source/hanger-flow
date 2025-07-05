package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.task.access.FlowTaskErrorHandlingAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskErrorHandingAccessContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture任务错误处理访问接口实现
 */
public class CompletableFlowTaskErrorHandlingAccess implements FlowTaskErrorHandlingAccess {

    private final FlowTaskErrorHandingAccessContext context;

    public CompletableFlowTaskErrorHandlingAccess(FlowTaskErrorHandingAccessContext context) {
        this.context = context;
    }

    @Override
    public FlowTaskErrorHandingAccessContext getContext() {
        return context;
    }

    @Override
    public void log(String message) {
        var ctx = (source.hanger.flow.completable.runtime.context.CompletableFlowTaskErrorHandingAccessContext) getContext();
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
            ctx.getFlowContext().getFlowDefinition().getName(),
            ctx.getFlowContext().getFlowDefinition().getVersion(),
            ctx.getExecutionId(),
            ctx.getStepName()
        ), message);
    }

    @Override
    public Throwable getException() {
        return context.getException();
    }
} 