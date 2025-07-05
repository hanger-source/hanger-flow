package source.hanger.flow.core.runtime;

import source.hanger.flow.contract.model.StepDefinition;

/**
 * 步骤错误处理策略接口
 * <p>
 * 负责处理步骤执行过程中的异常，支持自定义错误处理逻辑。
 * 
 * 设计说明：
 * - 作为核心运行时接口，定义步骤错误处理的标准协议
 * - 支持不同业务场景的差异化错误处理
 * - 便于扩展和自定义错误处理策略
 */
public interface StepErrorHandler {
    /**
     * 处理步骤执行错误
     * @param step    出错的步骤
     * @param context 执行上下文
     * @param error   异常信息
     * @return 错误处理结果
     */
    FlowResult handleError(StepDefinition step, FlowExecutionContext context, Exception error);
} 