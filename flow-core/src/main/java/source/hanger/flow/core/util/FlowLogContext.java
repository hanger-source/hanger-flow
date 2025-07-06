package source.hanger.flow.core.util;

import java.util.Objects;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;

/**
 * 日志上下文对象，统一封装日志所需所有元信息
 */
public final class FlowLogContext {
    final String flowName;
    final String version;
    final String executionId;
    final String stepName;

    public FlowLogContext(FlowExecutionContext context, StepDefinition stepDefinition) {
        this(context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            context.getExecutionId(),
            stepDefinition.getName());
    }

    public FlowLogContext(FlowExecutionContext context, String stepName) {
        this(context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            context.getExecutionId(),
            stepName);
    }

    /**
     *
     */
    public FlowLogContext(String flowName, String version, String executionId, String stepName) {
        this.flowName = flowName;
        this.version = version;
        this.executionId = executionId;
        this.stepName = stepName;
    }

    public FlowLogContext stepName(String stepName) {
        return new FlowLogContext(flowName, version, executionId, stepName);
    }

    public String flowName() {return flowName;}

    public String version() {return version;}

    public String executionId() {return executionId;}

    public String stepName() {return stepName;}

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {return true;}
        if (obj == null || obj.getClass() != getClass()) {return false;}
        var that = (FlowLogContext)obj;
        return Objects.equals(flowName, that.flowName) &&
            Objects.equals(version, that.version) &&
            Objects.equals(executionId, that.executionId) &&
            Objects.equals(stepName, that.stepName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowName, version, executionId, stepName);
    }

    @Override
    public String toString() {
        return "FlowLogContext[" +
            "flowName=" + flowName + ", " +
            "version=" + version + ", " +
            "executionId=" + executionId + ", " +
            "stepName=" + stepName + ']';
    }

}
