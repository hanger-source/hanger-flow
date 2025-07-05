package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.flow.access.FlowEnterHandlingAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowEnterHandingAccessContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture流程进入处理访问接口实现
 */
public class CompletableFlowEnterHandlingAccess implements FlowEnterHandlingAccess {

    private final FlowEnterHandingAccessContext context;

    public CompletableFlowEnterHandlingAccess(FlowEnterHandingAccessContext context) {
        this.context = context;
    }

    @Override
    public FlowEnterHandingAccessContext getContext() {
        return context;
    }

    @Override
    public void log(String message) {
        var ctx = (source.hanger.flow.completable.runtime.context.CompletableFlowEnterHandingAccessContext) getContext();
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
            ctx.getFlowContext().getFlowDefinition().getName(),
            ctx.getFlowContext().getFlowDefinition().getVersion(),
            ctx.getExecutionId(),
            ctx.getStepName()
        ), message);
    }
}