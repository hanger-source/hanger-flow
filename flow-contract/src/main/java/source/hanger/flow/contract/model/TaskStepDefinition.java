package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.common.FlowClosure;

/**
 * 任务步骤定义
 * 定义了任务步骤的基本属性和执行逻辑
 */
public class TaskStepDefinition extends AbstractStepDefinition {
    private FlowClosure taskClosure;
    private FlowClosure enterHandlingClosure;
    private FlowClosure errorHandlingClosure;
    /** 是否支持流式输出 */
    private boolean streamingSupported = false;
    /** 输出类型 */
    private Class<?> outputType = Object.class;

    public TaskStepDefinition() {
        super();
    }

    public TaskStepDefinition(String name) {
        super();
        setName(name);
    }

    public TaskStepDefinition(String name, String description) {
        super();
        setName(name);
        setDescription(description);
    }

    /**
     * 获取任务执行闭包
     */
    public FlowClosure getTaskClosure() {
        return taskClosure;
    }

    /**
     * 设置任务执行闭包
     */
    public void setTaskClosure(FlowClosure taskClosure) {
        this.taskClosure = taskClosure;
    }

    /**
     * 获取任务进入处理闭包
     */
    public FlowClosure getEnterHandlingClosure() {
        return enterHandlingClosure;
    }

    /**
     * 设置任务进入处理闭包
     */
    public void setEnterHandlingClosure(FlowClosure enterHandlingClosure) {
        this.enterHandlingClosure = enterHandlingClosure;
    }

    /**
     * 获取任务错误处理闭包
     */
    public FlowClosure getErrorHandlingClosure() {
        return errorHandlingClosure;
    }

    /**
     * 设置任务错误处理闭包
     */
    public void setErrorHandlingClosure(FlowClosure errorHandlingClosure) {
        this.errorHandlingClosure = errorHandlingClosure;
    }

    // 兼容性方法，保持向后兼容
    public void setTaskRunnable(FlowClosure taskClosure) {
        this.taskClosure = taskClosure;
    }

    public FlowClosure getTaskRunnable() {
        return taskClosure;
    }

    public void setEnterHandingRunnable(FlowClosure enterHandlingClosure) {
        this.enterHandlingClosure = enterHandlingClosure;
    }

    public FlowClosure getEnterHandingRunnable() {
        return enterHandlingClosure;
    }

    public void setErrorHandingRunnable(FlowClosure errorHandlingClosure) {
        this.errorHandlingClosure = errorHandlingClosure;
    }

    public FlowClosure getErrorHandingRunnable() {
        return errorHandlingClosure;
    }

    @Override
    public boolean isStreamingSupported() {
        return streamingSupported;
    }

    public void setStreamingSupported(boolean streamingSupported) {
        this.streamingSupported = streamingSupported;
    }

    @Override
    public Class<?> getOutputType() {
        return outputType;
    }

    public void setOutputType(Class<?> outputType) {
        this.outputType = outputType;
    }

    @Override
    public StepType getStepType() {
        return StepType.TASK;
    }
}