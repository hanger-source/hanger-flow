package source.hanger.flow.core.runtime.engine;

import source.hanger.flow.core.runtime.execution.FlowResult;

import java.util.concurrent.CompletableFuture;

public interface FlowExecutionHandle {
    /**
     * 获取流程实例ID
     */
    String executionId();

    /**
     * 获取流程最终结果的Future
     */
    CompletableFuture<FlowResult> future();

    /**
     * 查询流程当前状态
     */
    FlowEngine.FlowExecutionState getStatus();

    /**
     * 尝试中断/停止流程
     */
    boolean stop();
}