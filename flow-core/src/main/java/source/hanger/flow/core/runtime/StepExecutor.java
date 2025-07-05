package source.hanger.flow.core.runtime;

import source.hanger.flow.contract.model.StepDefinition;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 步骤执行器接口
 * <p>
 * 负责根据步骤类型执行具体的流程节点。
 * 支持异步执行，便于扩展不同类型的节点（任务、并行、异步等）。
 * 
 * 设计说明：
 * - 作为核心运行时接口，定义步骤执行的标准协议
 * - 支持不同运行时实现（如CompletableFuture、Reactor等）
 * - 便于扩展新的步骤类型和执行策略
 */
public interface StepExecutor {
    /**
     * 执行指定步骤
     * @param step    步骤定义
     * @param context 执行上下文
     * @param executor 线程池
     * @return 异步执行结果
     */
    CompletableFuture<FlowResult> execute(StepDefinition step, FlowExecutionContext context, Executor executor);
} 