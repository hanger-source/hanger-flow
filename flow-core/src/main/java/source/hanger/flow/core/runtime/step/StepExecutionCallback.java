package source.hanger.flow.core.runtime.step;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.data.FlowData;
import source.hanger.flow.core.runtime.data.FlowData;

/**
 * StepExecutionCallback 定义步骤执行的回调接口，支持流式数据处理。
 *
 * 设计理念：
 * 1. 与 flow-contract 模型兼容 - 使用 StepDefinition
 * 2. 支持流式回调 - 处理 FlowData.fragment() 的中间数据
 * 3. 生命周期管理 - 处理开始、完成、异常等状态
 * 4. 进度报告 - 实时反馈执行进度
 * 5. 统一异常处理 - 处理各种异常场景
 *
 * @param <T> 步骤输出的数据类型
 */
public interface StepExecutionCallback<T> {

    /**
     * 创建基于 FlowData 的适配器回调。
     * 将传统的 onSuccess/onFailure 回调适配为 FlowData 回调。
     *
     * @param onSuccess 成功回调
     * @param onFailure 失败回调
     * @param <T>       数据类型
     * @return 适配后的 StepExecutionCallback
     */
    static <T> StepExecutionCallback<T> adapt(
        java.util.function.Consumer<T> onSuccess,
        java.util.function.Consumer<Throwable> onFailure) {
        return new StepExecutionCallback<T>() {
            @Override
            public void onStepCompleted(StepDefinition stepDefinition, T result) {
                onSuccess.accept(result);
            }

            @Override
            public void onStepFailed(StepDefinition stepDefinition, Throwable error) {
                onFailure.accept(error);
            }
        };
    }

    /**
     * 创建只处理最终结果的简单回调。
     *
     * @param onResult 结果处理回调
     * @param <T>      数据类型
     * @return 简化的 StepExecutionCallback
     */
    static <T> StepExecutionCallback<T> simple(java.util.function.Consumer<T> onResult) {
        return new StepExecutionCallback<T>() {
            @Override
            public void onStepCompleted(StepDefinition stepDefinition, T result) {
                onResult.accept(result);
            }
        };
    }

    /**
     * 创建空实现回调。
     * 用于不需要回调的场景。
     *
     * @param <T> 数据类型
     * @return 空实现的 StepExecutionCallback
     */
    static <T> StepExecutionCallback<T> noop() {
        return new StepExecutionCallback<T>() {};
    }

    /**
     * 步骤开始执行时的回调。
     * 可用于初始化、日志记录、进度跟踪等。
     *
     * @param stepDefinition 步骤定义
     * @param input          输入数据
     */
    default void onStepStarted(StepDefinition stepDefinition, Object input) {
        // 默认空实现，子类可重写
    }

    /**
     * 接收到流式数据片段时的回调。
     * 用于处理 LLM 逐字输出、进度报告、中间结果等。
     *
     * @param stepDefinition 步骤定义
     * @param fragment       数据片段
     */
    default void onFragment(StepDefinition stepDefinition, T fragment) {
        // 默认空实现，子类可重写
    }

    /**
     * 步骤执行完成时的回调。
     * 表示步骤正常结束，携带最终结果。
     *
     * @param stepDefinition 步骤定义
     * @param result         最终结果
     */
    default void onStepCompleted(StepDefinition stepDefinition, T result) {
        // 默认空实现，子类可重写
    }

    /**
     * 步骤执行异常时的回调。
     * 表示步骤异常终止，携带异常信息。
     *
     * @param stepDefinition 步骤定义
     * @param error          异常信息
     */
    default void onStepFailed(StepDefinition stepDefinition, Throwable error) {
        // 默认空实现，子类可重写
    }

    /**
     * 步骤执行进度报告。
     * 用于实时反馈执行进度，如百分比、状态描述等。
     *
     * @param stepDefinition 步骤定义
     * @param progress       进度信息（可以是任意类型）
     */
    default void onProgress(StepDefinition stepDefinition, Object progress) {
        // 默认空实现，子类可重写
    }

    /**
     * 统一的 FlowData 处理回调。
     * 这是一个高级接口，用于处理完整的 FlowData 对象。
     * 默认实现会根据 FlowData 类型调用相应的回调方法。
     *
     * @param stepDefinition 步骤定义
     * @param flowData       FlowData 对象
     */
    default void onFlowData(StepDefinition stepDefinition, FlowData<T> flowData) {
        if (flowData.isFragment()) {
            onFragment(stepDefinition, flowData.fragment());
        } else if (flowData.isDone()) {
            onStepCompleted(stepDefinition, flowData.result());
        } else if (flowData.isError()) {
            onStepFailed(stepDefinition, flowData.error());
        }
    }
} 