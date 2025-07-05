package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskRunAccessContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture任务执行访问接口实现
 */
public class CompletableFlowTaskRunAccess implements FlowTaskRunAccess {

    private final FlowTaskRunAccessContext context;

    public CompletableFlowTaskRunAccess(FlowTaskRunAccessContext context) {
        this.context = context;
    }

    @Override
    public FlowTaskRunAccessContext getContext() {
        return context;
    }

    @Override
    public void log(String message) {
        var ctx = (source.hanger.flow.completable.runtime.context.CompletableFlowTaskRunAccessContext) getContext();
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
            ctx.getFlowContext().getFlowDefinition().getName(),
            ctx.getFlowContext().getFlowDefinition().getVersion(),
            ctx.getExecutionId(),
            ctx.getStepName()
        ), message);
    }
}