package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.flow.access.FlowErrorHandlingAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowErrorHandingAccessContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture流程错误处理访问接口实现
 */
public class CompletableFlowErrorHandlingAccess implements FlowErrorHandlingAccess {

    private final FlowErrorHandingAccessContext context;

    public CompletableFlowErrorHandlingAccess(FlowErrorHandingAccessContext context) {
        this.context = context;
    }

    @Override
    public FlowErrorHandingAccessContext getContext() {
        return context;
    }

    @Override
    public void log(String message) {
        var ctx = (source.hanger.flow.completable.runtime.context.CompletableFlowErrorHandingAccessContext) getContext();
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