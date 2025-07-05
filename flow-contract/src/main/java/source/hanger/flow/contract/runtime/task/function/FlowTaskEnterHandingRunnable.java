package source.hanger.flow.contract.runtime.task.function;

import source.hanger.flow.contract.runtime.task.access.FlowTaskEnterHandingAccess;

/**
 * 任务进入处理接口
 * 定义了任务执行前的预处理逻辑，在任务正式执行前调用
 */
@FunctionalInterface
public interface FlowTaskEnterHandingRunnable {
    /**
     * 处理任务进入逻辑
     *
     * @param access 任务进入处理访问接口，提供任务进入时的上下文访问能力
     */
    void handle(FlowTaskEnterHandingAccess access);
}