package source.hanger.flow.core.runtime.step;

import java.util.concurrent.CompletableFuture;

/**
 * StepExecutionHandle 定义步骤执行的控制句柄。
 *
 * 设计理念：
 * 1. 提供执行控制 - 取消、暂停、恢复等操作
 * 2. 状态查询 - 获取执行状态、进度等信息
 * 3. 结果获取 - 获取执行结果或异常
 * 4. 运行时无关 - 不绑定特定的异步模型
 *
 * @param <T> 步骤输出的数据类型
 */
public interface StepExecutionHandle<T> {

    /**
     * 获取执行状态。
     *
     * @return 执行状态
     */
    ExecutionStatus getStatus();

    /**
     * 获取执行结果。
     * 只有在执行完成时才返回结果，否则返回 null。
     *
     * @return 执行结果，如果未完成则返回 null
     */
    T getResult();

    /**
     * 获取执行异常。
     * 只有在执行失败时才返回异常，否则返回 null。
     *
     * @return 执行异常，如果未失败则返回 null
     */
    Throwable getError();

    /**
     * 取消执行。
     * 尝试取消正在执行的步骤，具体行为取决于实现。
     *
     * @return 是否成功取消
     */
    boolean cancel();

    /**
     * 检查是否已取消。
     *
     * @return 是否已取消
     */
    boolean isCancelled();

    CompletableFuture<T> getFuture();

    /**
     * 执行状态枚举。
     */
    enum ExecutionStatus {
        /**
         * 等待执行
         */
        PENDING,
        /**
         * 正在执行
         */
        RUNNING,
        /**
         * 执行完成
         */
        COMPLETED,
        /**
         * 执行失败
         */
        FAILED,
        /**
         * 已取消
         */
        CANCELLED
    }

}