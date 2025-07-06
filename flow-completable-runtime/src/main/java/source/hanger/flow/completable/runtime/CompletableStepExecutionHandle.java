package source.hanger.flow.completable.runtime;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import source.hanger.flow.core.runtime.step.StepExecutionHandle;

/**
 * CompletableStepExecutionHandle 是 CompletableFuture 运行时对 StepExecutionHandle 的适配实现。
 *
 * 设计理念：
 * 1. 基于 CompletableFuture 提供异步执行控制
 * 2. 支持状态查询和进度跟踪
 * 3. 提供取消和等待功能
 * 4. 适配新的 StepExecutionHandle 接口
 *
 * @param <T> 步骤输出的数据类型
 */
public class CompletableStepExecutionHandle<T> implements StepExecutionHandle<T> {

    private final CompletableFuture<T> future;
    private final AtomicReference<ExecutionStatus> status;
    private final long startTime;

    public CompletableStepExecutionHandle(CompletableFuture<T> future) {
        this.future = future;
        status = new AtomicReference<>(ExecutionStatus.PENDING);
        startTime = System.currentTimeMillis();

        // 监听 CompletableFuture 状态变化
        future.whenComplete((result, throwable) -> {
            if (future.isCancelled()) {
                status.set(ExecutionStatus.CANCELLED);
            } else if (throwable != null) {
                status.set(ExecutionStatus.FAILED);
            } else {
                status.set(ExecutionStatus.COMPLETED);
            }
        });
    }

    /**
     * 创建基于 CompletableFuture 的句柄。
     *
     * @param future CompletableFuture 对象
     * @param <T>    数据类型
     * @return 适配后的 StepExecutionHandle
     */
    public static <T> StepExecutionHandle<T> fromCompletableFuture(CompletableFuture<T> future) {
        return new CompletableStepExecutionHandle<>(future);
    }

    /**
     * 创建已完成的句柄。
     *
     * @param result 结果
     * @param <T>    数据类型
     * @return 已完成的 StepExecutionHandle
     */
    public static <T> StepExecutionHandle<T> completed(T result) {
        return fromCompletableFuture(CompletableFuture.completedFuture(result));
    }

    /**
     * 创建失败的句柄。
     *
     * @param error 异常
     * @param <T>   数据类型
     * @return 失败的 StepExecutionHandle
     */
    public static <T> StepExecutionHandle<T> failed(Throwable error) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return fromCompletableFuture(future);
    }

    /**
     * 创建已取消的句柄。
     *
     * @param <T> 数据类型
     * @return 已取消的 StepExecutionHandle
     */
    public static <T> StepExecutionHandle<T> cancelled() {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.cancel(true);
        return fromCompletableFuture(future);
    }

    @Override
    public ExecutionStatus getStatus() {
        return status.get();
    }

    @Override
    public T getResult() {
        try {
            return future.get();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Throwable getError() {
        if (future.isCompletedExceptionally()) {
            try {
                future.get();
            } catch (Exception e) {
                return e.getCause() != null ? e.getCause() : e;
            }
        }
        return null;
    }

    @Override
    public boolean cancel() {
        return future.cancel(true);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /**
     * 获取底层的 CompletableFuture。
     *
     * @return CompletableFuture 对象
     */
    public CompletableFuture<T> getFuture() {
        return future;
    }
} 