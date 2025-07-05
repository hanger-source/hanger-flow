package source.hanger.flow.contract.model;

import java.util.ArrayList;
import java.util.List;

import source.hanger.flow.contract.runtime.flow.function.FlowEnterHandingRunnable;
import source.hanger.flow.contract.runtime.flow.function.FlowErrorHandingRunnable;

public class FlowDefinition {
    private String version;
    private String name;
    private String description;
    /**
     * 流程进入时的逻辑
     * 只有一次
     */
    private FlowEnterHandingRunnable enterHandingRunnable;
    /**
     * 异常处理逻辑
     * 只有一次
     */
    private FlowErrorHandingRunnable errorHandingRunnable;
    /**
     * 流程步骤
     */
    private List<StepDefinition> stepDefinitions;

    public void addStep(StepDefinition stepDefinition) {
        if (stepDefinitions == null) {
            stepDefinitions = new ArrayList<>();
        }
        stepDefinitions.add(stepDefinition);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FlowEnterHandingRunnable getEnterHandingRunnable() {
        return enterHandingRunnable;
    }

    public void setEnterHandingRunnable(
        FlowEnterHandingRunnable enterHandingRunnable) {
        this.enterHandingRunnable = enterHandingRunnable;
    }

    public FlowErrorHandingRunnable getErrorHandingRunnable() {
        return errorHandingRunnable;
    }

    public void setErrorHandingRunnable(
        FlowErrorHandingRunnable errorHandingRunnable) {
        this.errorHandingRunnable = errorHandingRunnable;
    }

    public List<StepDefinition> getStepDefinitions() {
        return stepDefinitions;
    }
}
