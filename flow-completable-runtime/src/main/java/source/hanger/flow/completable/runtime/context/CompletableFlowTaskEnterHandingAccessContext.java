package source.hanger.flow.completable.runtime.context;

import java.io.Serial;

import source.hanger.flow.contract.runtime.task.context.FlowTaskEnterHandingAccessContext;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture任务进入处理访问上下文实现
 */
public class CompletableFlowTaskEnterHandingAccessContext extends FlowTaskEnterHandingAccessContext {

    @Serial
    private static final long serialVersionUID = 4175212068525325292L;
    private final FlowExecutionContext flowContext;
    private final String stepName;

    public CompletableFlowTaskEnterHandingAccessContext(FlowExecutionContext flowContext, String stepName) {
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