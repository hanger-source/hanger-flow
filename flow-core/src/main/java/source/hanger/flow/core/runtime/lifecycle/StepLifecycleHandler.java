package source.hanger.flow.core.runtime.lifecycle;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;

/**
 * 步骤生命周期处理接口
 * <p>
 * 负责处理步骤级别的生命周期事件，如步骤开始、步骤完成、步骤错误等。
 * 
 * 设计说明：
 * - 作为核心运行时接口，定义步骤生命周期处理的标准协议
 * - 支持不同业务场景的差异化生命周期处理
 * - 便于扩展和自定义生命周期处理策略
 */
public interface StepLifecycleHandler {
    /**
     * 步骤开始执行
     * @param step    步骤定义
     * @param context 执行上下文
     */
    void onStepStart(StepDefinition step, FlowExecutionContext context);

    /**
     * 步骤执行完成
     * @param step    步骤定义
     * @param context 执行上下文
     */
    void onStepComplete(StepDefinition step, FlowExecutionContext context);

    /**
     * 步骤执行错误
     * @param step    步骤定义
     * @param context 执行上下文
     * @param error   错误信息
     */
    void onStepError(StepDefinition step, FlowExecutionContext context, Exception error);
} 