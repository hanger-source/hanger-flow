package source.hanger.flow.core.runtime.engine;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.runtime.common.FlowClosure;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.data.FlowData;
import source.hanger.flow.core.runtime.step.StepExecutor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * FlowEngine 定义流程引擎的核心接口。
 *
 * 设计理念：
 * 1. 与 flow-contract 模型保持一致 - 使用 FlowDefinition 和 StepDefinition
 * 2. 支持 FlowData 流式处理 - 统一的数据传输协议
 * 3. 运行时无关 - 不绑定特定的异步模型
 * 4. 向后兼容 - 保持与现有 DSL 的兼容性
 *
 * @param <T> 流程输出的数据类型
 */
public interface FlowEngine<T> {

    /**
     * 启动流程并返回Handle
     */
    FlowExecutionHandle start(FlowDefinition flowDefinition, Map<String, Object> initialParams);

    /**
     * 执行流程，返回 CompletableFuture。
     * 这是主要的执行接口，与 flow-contract 模型兼容。
     *
     * @param flowDefinition 流程定义（来自 flow-contract）
     * @param initialParams  初始参数
     * @return 执行结果的 CompletableFuture
     */
    default CompletableFuture<FlowResult> execute(FlowDefinition flowDefinition, Map<String, Object> initialParams) {
        return start(flowDefinition, initialParams).future();
    }

    /**
     * 执行流程，无初始参数。
     *
     * @param flowDefinition 流程定义
     * @return 执行结果的 CompletableFuture
     */
    default CompletableFuture<FlowResult> execute(FlowDefinition flowDefinition) {
        return execute(flowDefinition, Map.of());
    }

    /**
     * 执行流程，支持 FlowData 流式处理。
     * 这是一个高级接口，用于需要流式处理的场景。
     *
     * @param flowDefinition   流程定义
     * @param initialParams    初始参数
     * @param flowDataCallback 流式数据回调
     * @return 执行结果的 CompletableFuture
     */
    default CompletableFuture<FlowResult> executeWithFlowData(
        FlowDefinition flowDefinition,
        Map<String, Object> initialParams,
        FlowDataCallback<T> flowDataCallback) {
        return execute(flowDefinition, initialParams);
    }

    /**
     * 停止流程执行。
     *
     * @param executionId 执行ID
     * @return 是否成功停止
     */
    boolean stop(String executionId);

    /**
     * 获取流程执行状态。
     *
     * @param executionId 执行ID
     * @return 执行状态
     */
    FlowExecutionState getExecutionState(String executionId);

    /**
     * 流程执行状态枚举。
     */
    enum FlowExecutionState {
        /**
         * 等待执行
         */
        PENDING,
        /**
         * 正在执行
         */
        RUNNING,
        /**
         * 执行完成
         */
        COMPLETED,
        /**
         * 执行失败
         */
        FAILED,
        /**
         * 已停止
         */
        STOPPED
    }

    /**
     * FlowData 回调接口。
     * 用于处理流式数据。
     *
     * @param <T> 数据类型
     */
    interface FlowDataCallback<T> {
        /**
         * 接收到流式数据片段。
         *
         * @param stepName 步骤名称
         * @param flowData 流式数据
         */
        default void onFlowData(String stepName, FlowData<T> flowData) {
            // 默认空实现
        }

        /**
         * 流程开始执行。
         *
         * @param flowDefinition 流程定义
         */
        default void onFlowStarted(FlowDefinition flowDefinition) {
            // 默认空实现
        }

        /**
         * 步骤开始执行。
         *
         * @param stepDefinition 步骤定义
         */
        default void onStepStarted(StepDefinition stepDefinition) {
            // 默认空实现
        }

        /**
         * 步骤执行完成。
         *
         * @param stepDefinition 步骤定义
         * @param result         执行结果
         */
        default void onStepCompleted(StepDefinition stepDefinition, Object result) {
            // 默认空实现
        }

        /**
         * 步骤执行失败。
         *
         * @param stepDefinition 步骤定义
         * @param error          错误信息
         */
        default void onStepFailed(StepDefinition stepDefinition, Throwable error) {
            // 默认空实现
        }

        /**
         * 流程执行完成。
         *
         * @param flowDefinition 流程定义
         * @param result         执行结果
         */
        default void onFlowCompleted(FlowDefinition flowDefinition, FlowResult result) {
            // 默认空实现
        }

        /**
         * 流程执行失败。
         *
         * @param flowDefinition 流程定义
         * @param error          错误信息
         */
        default void onFlowFailed(FlowDefinition flowDefinition, Throwable error) {
            // 默认空实现
        }
    }

    /**
     * 步骤执行器工厂接口。
     * 用于创建特定类型的步骤执行器。
     */
    interface StepExecutorFactory {
        /**
         * 创建任务步骤执行器。
         *
         * @param taskRunnable 任务执行逻辑
         * @return 任务步骤执行器
         */
        StepExecutor<Object> createTaskExecutor(FlowClosure taskRunnable);

        /**
         * 创建并行步骤执行器。
         *
         * @param stepDefinitions 子步骤定义
         * @return 并行步骤执行器
         */
        StepExecutor<Object> createParallelExecutor(java.util.List<StepDefinition> stepDefinitions);

        /**
         * 创建异步步骤执行器。
         *
         * @param stepDefinitions 子步骤定义
         * @return 异步步骤执行器
         */
        StepExecutor<Object> createAsyncExecutor(java.util.List<StepDefinition> stepDefinitions);

        /**
         * 注册自定义步骤执行器。
         *
         * @param stepType 步骤类型
         * @param executor 步骤执行器
         */
        void registerExecutor(String stepType, StepExecutor<Object> executor);
    }
} 