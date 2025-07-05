package source.hanger.flow.contract.runtime.task.function;

import source.hanger.flow.contract.runtime.task.access.FlowTaskErrorHandlingAccess;

/**
 * 任务错误处理接口
 * 定义了任务执行过程中发生错误时的处理逻辑
 */
@FunctionalInterface
public interface FlowTaskErrorHandingRunnable {
    /**
     * 处理任务错误
     *
     * @param access 任务错误处理访问接口，提供错误处理时的上下文访问能力
     */
    void handle(FlowTaskErrorHandlingAccess access);
}