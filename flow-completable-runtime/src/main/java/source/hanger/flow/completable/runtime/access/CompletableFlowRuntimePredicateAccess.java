package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess;
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccessContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture流程运行时条件判断访问接口实现
 */
public class CompletableFlowRuntimePredicateAccess implements FlowRuntimePredicateAccess {

    private final FlowRuntimePredicateAccessContext context;

    public CompletableFlowRuntimePredicateAccess(FlowRuntimePredicateAccessContext context) {
        this.context = context;
    }

    @Override
    public FlowRuntimePredicateAccessContext getContext() {
        return context;
    }

    @Override
    public void log(String message) {
        var ctx = (source.hanger.flow.completable.runtime.context.CompletableFlowRuntimePredicateAccessContext) getContext();
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
            ctx.getFlowContext().getFlowDefinition().getName(),
            ctx.getFlowContext().getFlowDefinition().getVersion(),
            ctx.getExecutionId(),
            ctx.getStepName()
        ), message);
    }

}