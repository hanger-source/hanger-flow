package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.task.access.FlowTaskEnterHandingAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskEnterHandingAccessContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture任务进入处理访问接口实现
 */
public class CompletableFlowTaskEnterHandingAccess implements FlowTaskEnterHandingAccess {

    private final FlowTaskEnterHandingAccessContext context;

    public CompletableFlowTaskEnterHandingAccess(FlowTaskEnterHandingAccessContext context) {
        this.context = context;
    }

    @Override
    public FlowTaskEnterHandingAccessContext getContext() {
        return context;
    }

    @Override
    public void log(String message) {
        if (context instanceof source.hanger.flow.completable.runtime.context.CompletableFlowTaskEnterHandingAccessContext ctx) {
            FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
                ctx.getFlowContext().getFlowDefinition().getName(),
                ctx.getFlowContext().getFlowDefinition().getVersion(),
                ctx.getExecutionId(),
                ctx.getStepName()
            ), message);
        } else {
            FlowLogger.log(FlowLogger.Level.DEBUG,
                new FlowLogger.FlowLogContext("-", "-", context.getExecutionId(), context.getStepName()), message);
        }
    }
}