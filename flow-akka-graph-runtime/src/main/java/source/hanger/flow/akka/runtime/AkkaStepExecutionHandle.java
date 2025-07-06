package source.hanger.flow.akka.runtime;

import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.step.StepExecutionHandle;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Akka 步骤执行句柄实现
 * <p>
 * 使用 Akka Actor 实现异步执行，展示如何用消息驱动的方式实现 StepExecutionHandle。
 *
 * 设计说明：
 * - 作为 Akka runtime 的 StepExecutionHandle 实现
 * - 使用 Akka Actor 进行异步消息处理
 * - 展示如何将 Akka 的异步特性适配到统一的 StepExecutionHandle 接口
 */
public class AkkaStepExecutionHandle implements StepExecutionHandle<FlowResult> {

    private final CompletableFuture<FlowResult> future;
    private volatile boolean cancelled = false;

    public AkkaStepExecutionHandle(CompletableFuture<FlowResult> future) {
        this.future = future;
    }

    @Override
    public FlowResult getResult() {
        if (future.isDone() && !future.isCompletedExceptionally()) {
            try {
                return future.get();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public boolean cancel() {
        if (!cancelled && !future.isDone()) {
            cancelled = true;
            return future.cancel(true);
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled || future.isCancelled();
    }

    /**
     * 获取底层的 CompletableFuture
     *
     * @return CompletableFuture
     */
    public CompletableFuture<FlowResult> getFuture() {
        return future;
    }

    @Override
    public ExecutionStatus getStatus() {
        if (future.isCancelled()) {
            return ExecutionStatus.CANCELLED;
        } else if (future.isCompletedExceptionally()) {
            return ExecutionStatus.FAILED;
        } else if (future.isDone()) {
            return ExecutionStatus.COMPLETED;
        } else {
            return ExecutionStatus.RUNNING;
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
}