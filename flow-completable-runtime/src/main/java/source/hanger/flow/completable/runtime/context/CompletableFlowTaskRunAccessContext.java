package source.hanger.flow.completable.runtime.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.task.context.FlowTaskRunAccessContext;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture任务执行访问上下文实现
 */
public class CompletableFlowTaskRunAccessContext extends FlowTaskRunAccessContext {

    @Serial
    private static final long serialVersionUID = 693063723062384847L;
    private final FlowExecutionContext flowContext;
    private final String stepName;

    public CompletableFlowTaskRunAccessContext(FlowExecutionContext flowContext, String stepName) {
        this.flowContext = flowContext;
        this.stepName = stepName;
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

    public java.util.Map<String, java.io.Serializable> getParams() {
        return flowContext.getParams();
    }
} 