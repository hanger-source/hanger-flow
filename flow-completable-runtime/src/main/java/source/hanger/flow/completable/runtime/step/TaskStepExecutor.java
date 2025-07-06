package source.hanger.flow.completable.runtime.step;

import source.hanger.flow.completable.runtime.CompletableStepExecutionHandle;
import source.hanger.flow.core.runtime.data.FlowData;
import source.hanger.flow.core.runtime.status.FlowStatus;
import source.hanger.flow.core.runtime.step.StepExecutionCallback;
import source.hanger.flow.core.runtime.step.StepExecutionHandle;
import source.hanger.flow.core.runtime.step.StepExecutor;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.runtime.common.FlowClosure;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimeExecuteContext;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimeExecuteAccess;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static source.hanger.flow.core.runtime.status.FlowStatus.SUCCESS;

/**
 * TaskStepExecutor 是基于 CompletableFuture 的任务步骤执行器。
 *
 * 设计理念：
 * 1. 与 flow-contract 模型兼容 - 使用 TaskStepDefinition 和 FlowTaskRunnable
 * 2. 支持 FlowData 流式处理 - 统一的数据传输协议
 * 3. 提供进度报告和生命周期管理
 * 4. 支持自定义执行器和异常处理
 */
public class TaskStepExecutor implements StepExecutor<FlowResult> {

    private final Executor executor;
    private final boolean enableStreaming;
    private final int streamingInterval;

    public TaskStepExecutor() {
        this(null, false, 100);
    }

    public TaskStepExecutor(Executor executor) {
        this(executor, false, 100);
    }

    public TaskStepExecutor(Executor executor, boolean enableStreaming, int streamingInterval) {
        this.executor = executor;
        this.enableStreaming = enableStreaming;
        this.streamingInterval = streamingInterval;
    }

    /**
     * 创建流式任务执行器。
     *
     * @param executor          执行器
     * @param streamingInterval 流式输出间隔（毫秒）
     * @return 流式任务执行器
     */
    public static TaskStepExecutor streaming(Executor executor, int streamingInterval) {
        return new TaskStepExecutor(executor, true, streamingInterval);
    }

    @Override
    public StepExecutionHandle<FlowResult> execute(StepDefinition stepDefinition, FlowExecutionContext context,
        StepExecutionCallback<FlowResult> callback) {
        if (!(stepDefinition instanceof TaskStepDefinition taskStep)) {
            throw new IllegalArgumentException("TaskStepExecutor只支持TaskStepDefinition类型");
        }

        String stepName = taskStep.getName();
        // 通知步骤开始
        callback.onStepStarted(stepDefinition, context.getInputs());
        CompletableFuture<FlowResult> future = new CompletableFuture<>();
        // 创建包装的callback，用于完成future
        StepExecutionCallback<FlowResult> wrappedCallback = new MyStepExecutionCallback(callback, future);

        CompletableFlowRuntimeExecuteContext accessContext
            = new CompletableFlowRuntimeExecuteContext(context, taskStep);
        CompletableFlowRuntimeExecuteAccess access
            = new CompletableFlowRuntimeExecuteAccess(accessContext, wrappedCallback);

        // 创建执行任务
        Runnable task = () -> {
            access.log("执行任务: ", taskStep);
            try {
                // 执行任务进入回调（onEnter）
                if (taskStep.getEnterHandingRunnable() != null) {
                    taskStep.getEnterHandingRunnable().call(access);
                }
                // 执行任务主体
                FlowClosure taskRunnable = taskStep.getTaskRunnable();
                if (taskRunnable != null) {
                    access.log("[TaskStepExecutor] 开始执行任务: " + stepName);
                    try {
                        taskRunnable.call(access);
                        // 写回 context 变更
                        context.getAttributes().putAll(accessContext);
                        access.log("[TaskStepExecutor] 任务执行完成");
                    } catch (Exception e) {
                        access.log("[TaskStepExecutor] 任务执行过程中抛出异常: {}", e.getMessage());
                        throw e;  // 重新抛出异常，让外层catch处理
                    }
                }

                // 任务执行完成后，如果future还没有完成，则通过callback完成
                if (!future.isDone()) {
                    access.log("[TaskStepExecutor] 任务未主动完成，通过callback完成future");
                    FlowResult result = FlowResult.success(context, taskStep.getName());
                    // 只通过callback完成，避免重复完成
                    wrappedCallback.onStepCompleted(stepDefinition, result);
                } else {
                    access.log("[TaskStepExecutor] 任务已主动完成");
                }
            } catch (Exception e) {
                access.log("[TaskStepExecutor] 运行任务异常: {}", e.getMessage());
                // 执行任务错误回调（onError）
                if (taskStep.getErrorHandingRunnable() != null) {
                    taskStep.getErrorHandingRunnable().call(access);
                }
                callback.onStepFailed(stepDefinition, e);
                future.completeExceptionally(e);
            }
        };

        // 使用指定的执行器或默认执行器
        if (executor != null) {
            executor.execute(task);
        } else {
            CompletableFuture.runAsync(task);
        }

        return new CompletableStepExecutionHandle<>(future);
    }

    private record MyStepExecutionCallback(StepExecutionCallback<FlowResult> callback,
                                           CompletableFuture<FlowResult> future)
        implements StepExecutionCallback<FlowResult> {

        @Override
        public void onStepStarted(StepDefinition stepDefinition, Object input) {
            callback.onStepStarted(stepDefinition, input);
        }

        @Override
        public void onStepCompleted(StepDefinition stepDefinition, FlowResult result) {
            callback.onStepCompleted(stepDefinition, result);
            future.complete(result);
        }

        @Override
        public void onStepFailed(StepDefinition stepDefinition, Throwable error) {
            callback.onStepFailed(stepDefinition, error);
            future.completeExceptionally(error);
        }

        @Override
        public void onFragment(StepDefinition stepDefinition, FlowResult fragment) {
            callback.onFragment(stepDefinition, fragment);
        }

        @Override
        public void onProgress(StepDefinition stepDefinition, Object progress) {
            callback.onProgress(stepDefinition, progress);
        }

        @Override
        public void onFlowData(StepDefinition stepDefinition, FlowData<FlowResult> flowData) {
            callback.onFlowData(stepDefinition, flowData);
        }
    }
}