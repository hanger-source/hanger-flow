package source.hanger.flow.completable.runtime.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.flow.context.FlowEnterHandingAccessContext;
import source.hanger.flow.core.runtime.FlowExecutionContext;

/**
 * CompletableFuture流程进入处理访问上下文实现
 */
public class CompletableFlowEnterHandingAccessContext extends FlowEnterHandingAccessContext {

    @Serial
    private static final long serialVersionUID = 6473473019359414640L;
    private final FlowExecutionContext flowContext;

    public CompletableFlowEnterHandingAccessContext(FlowExecutionContext flowContext) {
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