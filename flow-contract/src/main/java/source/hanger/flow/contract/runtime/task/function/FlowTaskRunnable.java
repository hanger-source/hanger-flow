package source.hanger.flow.contract.runtime.task.function;

import source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess;

/**
 * 任务执行接口
 * 定义了任务执行的核心逻辑，是任务步骤的主要执行入口
 */
@FunctionalInterface
public interface FlowTaskRunnable {
    /**
     * 执行任务
     *
     * @param access 任务执行访问接口，提供任务执行过程中的上下文访问能力
     */
    void run(FlowTaskRunAccess access);
}