package source.hanger.flow.core.runtime.error;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowResult;

/**
 * 流程错误处理策略接口
 * <p>
 * 负责处理流程级别的异常，支持自定义错误处理逻辑。
 * 
 * 设计说明：
 * - 作为核心运行时接口，定义流程错误处理的标准协议
 * - 支持不同业务场景的差异化错误处理
 * - 便于扩展和自定义错误处理策略
 */
public interface FlowErrorHandler {
    /**
     * 处理流程执行错误
     * @param flowDefinition 流程定义
     * @param context       执行上下文
     * @param error         异常信息
     * @return 错误处理结果
     */
    FlowResult handleError(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error);
} 