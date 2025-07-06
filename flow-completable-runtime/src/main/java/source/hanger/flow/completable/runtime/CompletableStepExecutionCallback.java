package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.data.FlowData;
import source.hanger.flow.core.runtime.step.StepExecutionCallback;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * CompletableStepExecutionCallback 是 CompletableFuture 运行时对 StepExecutionCallback 的适配实现。
 *
 * 设计理念：
 * 1. 适配 CompletableFuture 的回调机制
 * 2. 支持 FlowData 流式处理
 * 3. 提供传统回调的兼容性
 * 4. 支持进度报告和生命周期管理
 *
 * @param <T> 步骤输出的数据类型
 */
public class CompletableStepExecutionCallback<T> implements StepExecutionCallback<T> {

    private final Consumer<T> onSuccess;
    private final Consumer<Throwable> onFailure;
    private final Consumer<T> onFragment;
    private final Consumer<Object> onProgress;
    private final Consumer<Object> onStarted;

    private CompletableStepExecutionCallback(Builder<T> builder) {
        onSuccess = builder.onSuccess;
        onFailure = builder.onFailure;
        onFragment = builder.onFragment;
        onProgress = builder.onProgress;
        onStarted = builder.onStarted;
    }

    /**
     * 创建基于 CompletableFuture 的适配器。
     *
     * @param future CompletableFuture 对象
     * @param <T>    数据类型
     * @return 适配后的 StepExecutionCallback
     */
    public static <T> StepExecutionCallback<T> fromCompletableFuture(CompletableFuture<T> future) {
        return new StepExecutionCallback<T>() {
            @Override
            public void onStepCompleted(StepDefinition stepDefinition, T result) {
                future.complete(result);
            }

            @Override
            public void onStepFailed(StepDefinition stepDefinition, Throwable error) {
                future.completeExceptionally(error);
            }
        };
    }

    /**
     * 创建简单的成功/失败回调。
     *
     * @param onSuccess 成功回调
     * @param onFailure 失败回调
     * @param <T>       数据类型
     * @return 简化的 StepExecutionCallback
     */
    public static <T> StepExecutionCallback<T> simple(Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        return new Builder<T>()
            .onSuccess(onSuccess)
            .onFailure(onFailure)
            .build();
    }

    /**
     * 创建只处理成功的回调。
     *
     * @param onSuccess 成功回调
     * @param <T>       数据类型
     * @return 只处理成功的 StepExecutionCallback
     */
    public static <T> StepExecutionCallback<T> successOnly(Consumer<T> onSuccess) {
        return new Builder<T>()
            .onSuccess(onSuccess)
            .build();
    }

    /**
     * 创建只处理失败的回调。
     *
     * @param onFailure 失败回调
     * @param <T>       数据类型
     * @return 只处理失败的 StepExecutionCallback
     */
    public static <T> StepExecutionCallback<T> failureOnly(Consumer<Throwable> onFailure) {
        return new Builder<T>()
            .onFailure(onFailure)
            .build();
    }

    /**
     * 创建支持流式处理的回调。
     *
     * @param onFragment 片段回调
     * @param onSuccess  成功回调
     * @param onFailure  失败回调
     * @param <T>        数据类型
     * @return 支持流式处理的 StepExecutionCallback
     */
    public static <T> StepExecutionCallback<T> streaming(
        Consumer<T> onFragment,
        Consumer<T> onSuccess,
        Consumer<Throwable> onFailure) {
        return new Builder<T>()
            .onFragment(onFragment)
            .onSuccess(onSuccess)
            .onFailure(onFailure)
            .build();
    }

    @Override
    public void onStepStarted(StepDefinition stepDefinition, Object input) {
        if (onStarted != null) {
            onStarted.accept(input);
        }
    }

    @Override
    public void onFragment(StepDefinition stepDefinition, T fragment) {
        if (onFragment != null) {
            onFragment.accept(fragment);
        }
    }

    @Override
    public void onStepCompleted(StepDefinition stepDefinition, T result) {
        if (onSuccess != null) {
            onSuccess.accept(result);
        }
    }

    @Override
    public void onStepFailed(StepDefinition stepDefinition, Throwable error) {
        if (onFailure != null) {
            onFailure.accept(error);
        }
    }

    @Override
    public void onProgress(StepDefinition stepDefinition, Object progress) {
        if (onProgress != null) {
            onProgress.accept(progress);
        }
    }

    @Override
    public void onFlowData(StepDefinition stepDefinition, FlowData<T> flowData) {
        if (flowData.isFragment()) {
            onFragment(stepDefinition, flowData.fragment());
        } else if (flowData.isDone()) {
            onStepCompleted(stepDefinition, flowData.result());
        } else if (flowData.isError()) {
            onStepFailed(stepDefinition, flowData.error());
        }
    }

    /**
     * 构建器类，用于创建 CompletableStepExecutionCallback。
     *
     * @param <T> 数据类型
     */
    public static class Builder<T> {
        private Consumer<T> onSuccess;
        private Consumer<Throwable> onFailure;
        private Consumer<T> onFragment;
        private Consumer<Object> onProgress;
        private Consumer<Object> onStarted;

        public Builder<T> onSuccess(Consumer<T> onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        public Builder<T> onFailure(Consumer<Throwable> onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        public Builder<T> onFragment(Consumer<T> onFragment) {
            this.onFragment = onFragment;
            return this;
        }

        public Builder<T> onProgress(Consumer<Object> onProgress) {
            this.onProgress = onProgress;
            return this;
        }

        public Builder<T> onStarted(Consumer<Object> onStarted) {
            this.onStarted = onStarted;
            return this;
        }

        public CompletableStepExecutionCallback<T> build() {
            return new CompletableStepExecutionCallback<>(this);
        }
    }
} 