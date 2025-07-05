package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.task.function.FlowTaskEnterHandingRunnable;
import source.hanger.flow.contract.runtime.task.function.FlowTaskErrorHandingRunnable;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;

/**
 * 任务步骤定义类
 * 继承自AbstractStepDefinition，定义了具体的任务执行逻辑
 * 包含任务执行、进入处理和错误处理三个主要生命周期方法
 */
public class TaskStepDefinition extends AbstractStepDefinition {
    /** 任务执行逻辑 */
    private FlowTaskRunnable taskRunnable;
    /**
     * 任务进入时的逻辑
     * 只有一次
     */
    private FlowTaskEnterHandingRunnable enterHandingRunnable;
    /**
     * 任务异常处理逻辑
     * 只有一次
     */
    private FlowTaskErrorHandingRunnable errorHandingRunnable;

    /**
     * 获取任务进入处理逻辑
     * 
     * @return 任务进入处理逻辑
     */
    public FlowTaskEnterHandingRunnable getEnterHandingRunnable() {
        return enterHandingRunnable;
    }

    /**
     * 设置任务进入处理逻辑
     * 
     * @param enterHandingRunnable 任务进入处理逻辑
     */
    public void setEnterHandingRunnable(
        FlowTaskEnterHandingRunnable enterHandingRunnable) {
        this.enterHandingRunnable = enterHandingRunnable;
    }

    /**
     * 获取任务错误处理逻辑
     * 
     * @return 任务错误处理逻辑
     */
    public FlowTaskErrorHandingRunnable getErrorHandingRunnable() {
        return errorHandingRunnable;
    }

    /**
     * 设置任务错误处理逻辑
     * 
     * @param errorHandingRunnable 任务错误处理逻辑
     */
    public void setErrorHandingRunnable(
        FlowTaskErrorHandingRunnable errorHandingRunnable) {
        this.errorHandingRunnable = errorHandingRunnable;
    }

    /**
     * 设置任务执行逻辑
     * 
     * @param taskRunnable 任务执行逻辑
     */
    public void setTaskRunnable(FlowTaskRunnable taskRunnable) {
        this.taskRunnable = taskRunnable;
    }
}