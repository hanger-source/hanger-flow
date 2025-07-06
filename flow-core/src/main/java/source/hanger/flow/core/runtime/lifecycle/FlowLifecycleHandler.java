package source.hanger.flow.core.runtime.lifecycle;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;

/**
 * 流程生命周期处理接口
 * <p>
 * 负责处理流程级别的生命周期事件，如流程进入、流程完成、流程错误等。
 *
 * 设计说明：
 * - 作为核心运行时接口，定义流程生命周期处理的标准协议
 * - 支持不同业务场景的差异化生命周期处理
 * - 便于扩展和自定义生命周期处理策略
 */
public interface FlowLifecycleHandler {
    /**
     * 流程开始执行
     *
     * @param flowDefinition 流程定义
     * @param context        执行上下文
     */
    void onFlowStart(FlowDefinition flowDefinition, FlowExecutionContext context);

    /**
     * 流程执行完成
     *
     * @param flowDefinition 流程定义
     * @param context        执行上下文
     */
    void onFlowComplete(FlowDefinition flowDefinition, FlowExecutionContext context);

    /**
     * 流程执行错误
     *
     * @param flowDefinition 流程定义
     * @param context        执行上下文
     * @param error          错误信息
     */
    void onFlowError(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error);
} 