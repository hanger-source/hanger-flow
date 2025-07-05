package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.task.function.FlowTaskEnterHandingRunnable;
import source.hanger.flow.contract.runtime.task.function.FlowTaskErrorHandingRunnable;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;

public class TaskStepDefinition extends AbstractStepDefinition {
    private FlowTaskRunnable taskRunnable;
    /**
     * 流程进入时的逻辑
     * 只有一次
     */
    private FlowTaskEnterHandingRunnable enterHandingRunnable;
    /**
     * 异常处理逻辑
     * 只有一次
     */
    private FlowTaskErrorHandingRunnable errorHandingRunnable;

    public FlowTaskEnterHandingRunnable getEnterHandingRunnable() {
        return enterHandingRunnable;
    }

    public void setEnterHandingRunnable(
        FlowTaskEnterHandingRunnable enterHandingRunnable) {
        this.enterHandingRunnable = enterHandingRunnable;
    }

    public FlowTaskErrorHandingRunnable getErrorHandingRunnable() {
        return errorHandingRunnable;
    }

    public void setErrorHandingRunnable(
        FlowTaskErrorHandingRunnable errorHandingRunnable) {
        this.errorHandingRunnable = errorHandingRunnable;
    }

    public void setTaskRunnable(FlowTaskRunnable taskRunnable) {
        this.taskRunnable = taskRunnable;
    }
}