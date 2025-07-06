package source.hanger.flow.completable.runtime.context;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteContext;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import org.apache.commons.lang3.SerializationUtils;

/**
 * CompletableFuture统一执行上下文实现
 */
public class CompletableFlowRuntimeExecuteContext extends FlowRuntimeExecuteContext {

    @Serial
    private static final long serialVersionUID = 693063723062384847L;
    private final FlowExecutionContext flowContext;
    private final StepDefinition stepDefinition;

    public CompletableFlowRuntimeExecuteContext(FlowExecutionContext flowContext, String stepName) {
        this(flowContext, (StepDefinition)null, null);
        setStepName(stepName);
    }

    public CompletableFlowRuntimeExecuteContext(FlowExecutionContext flowContext,
        StepDefinition stepDefinition) {
        this(flowContext, stepDefinition, null);
    }

    public CompletableFlowRuntimeExecuteContext(FlowExecutionContext flowContext, String stepName,
        Throwable exception) {
        this(flowContext, (StepDefinition)null, exception);
        setStepName(stepName);
    }

    @SuppressWarnings("unchecked")
    private CompletableFlowRuntimeExecuteContext(FlowExecutionContext flowContext,
        StepDefinition stepDefinition,
        Throwable exception) {
        this.flowContext = flowContext;
        this.stepDefinition = stepDefinition;
        setVersion(flowContext.getFlowDefinition().getVersion());
        setExecutionId(flowContext.getExecutionId());
        setFlowName(flowContext.getFlowDefinition().getName());
        setException(exception);
        setChannel(flowContext.getChannel());
        if (stepDefinition != null) {
            setStepName(stepDefinition.getName());
        }
        Map<String, Object> clonedAttributes = (Map<String, Object>)SerializationUtils
            .clone((Serializable)flowContext.getAttributes());
        if (clonedAttributes != null) {
            putAll(clonedAttributes);
        }
    }

    /**
     * 获取流程执行上下文
     */
    public FlowExecutionContext getFlowContext() {
        return flowContext;
    }

    /**
     * 获取步骤定义
     */
    public StepDefinition getStepDefinition() {
        return stepDefinition;
    }

    @Override
    public Map<String, Object> getInputs() {
        return flowContext.getInputs();
    }

    @Override
    public Object getInput(String key) {
        return flowContext.getInput(key);
    }

    @Override
    public Object getInput(String key, Object defaultValue) {
        return flowContext.getInput(key, defaultValue);
    }
}