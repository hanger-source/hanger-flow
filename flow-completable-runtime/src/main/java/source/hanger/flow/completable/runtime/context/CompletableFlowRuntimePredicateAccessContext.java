package source.hanger.flow.completable.runtime.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccessContext;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture流程运行时条件判断访问上下文实现
 */
public class CompletableFlowRuntimePredicateAccessContext extends FlowRuntimePredicateAccessContext {

    @Serial
    private static final long serialVersionUID = 6423231669058029678L;
    private final FlowExecutionContext flowContext;

    public CompletableFlowRuntimePredicateAccessContext(FlowExecutionContext flowContext) {
        this.flowContext = flowContext;
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