package source.hanger.flow.core.runtime.step;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;

/**
 * StepExecutor 定义步骤执行的核心接口。
 *
 * 设计理念：
 * 1. 与 flow-contract 模型兼容 - 使用 StepDefinition 和 FlowTaskRunnable
 * 2. 支持 FlowData 流式处理 - 统一的数据传输协议
 * 3. 运行时无关 - 不绑定特定的异步模型
 * 4. 向后兼容 - 保持与现有 DSL 的兼容性
 *
 * @param <T> 步骤输出的数据类型
 */
public interface StepExecutor<T> {

    /**
     * 执行步骤，通过回调通知结果。
     *
     * @param stepDefinition 步骤定义（来自 flow-contract）
     * @param context        执行上下文
     * @param callback       执行回调
     * @return 执行句柄，用于控制执行过程
     */
    StepExecutionHandle<T> execute(StepDefinition stepDefinition, FlowExecutionContext context,
        StepExecutionCallback<T> callback);

    /**
     * 执行步骤，返回执行句柄。
     * 适用于不需要回调的场景。
     *
     * @param stepDefinition 步骤定义
     * @param context        执行上下文
     * @return 执行句柄
     */
    default StepExecutionHandle<T> execute(StepDefinition stepDefinition, FlowExecutionContext context) {
        return execute(stepDefinition, context, StepExecutionCallback.noop());
    }

    /**
     * 执行步骤，支持 FlowData 流式处理。
     * 这是一个高级接口，用于需要流式处理的场景。
     *
     * @param stepDefinition   步骤定义
     * @param context          执行上下文
     * @param flowDataCallback 流式数据回调
     * @return 执行句柄
     */
    default StepExecutionHandle<T> executeWithFlowData(StepDefinition stepDefinition,
        FlowExecutionContext context,
        StepExecutionCallback<T> flowDataCallback) {
        return execute(stepDefinition, context, flowDataCallback);
    }

} 