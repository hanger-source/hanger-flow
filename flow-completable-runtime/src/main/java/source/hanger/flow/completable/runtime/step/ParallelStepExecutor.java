package source.hanger.flow.completable.runtime.step;

import source.hanger.flow.completable.runtime.CompletableStepExecutionHandle;
import source.hanger.flow.contract.model.ParallelStepDefinition;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.status.FlowStatus;
import source.hanger.flow.core.runtime.step.StepExecutionCallback;
import source.hanger.flow.core.runtime.step.StepExecutionHandle;
import source.hanger.flow.core.runtime.step.StepExecutor;
import source.hanger.flow.core.util.FlowLogContext;
import source.hanger.flow.core.util.FlowLogger;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimeExecuteAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimeExecuteContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static source.hanger.flow.core.util.FlowStructureUtils.*;

/**
 * 并行节点执行器
 * <p>
 * 专门负责ParallelStepDefinition的执行逻辑。
 * 兼容新的抽象接口，返回 StepExecutionHandle。
 */
public class ParallelStepExecutor implements StepExecutor<FlowResult> {

    private final Executor executor;
    private final TaskStepExecutor taskStepExecutor = new TaskStepExecutor();

    public ParallelStepExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public StepExecutionHandle<FlowResult> execute(StepDefinition step, FlowExecutionContext context,
        StepExecutionCallback<FlowResult> callback) {
        if (!(step instanceof ParallelStepDefinition parallelStep)) {
            throw new IllegalArgumentException("ParallelStepExecutor只支持ParallelStepDefinition类型");
        }
        String stepName = parallelStep.getName();
        // 通知步骤开始
        callback.onStepStarted(step, context.getInputs());
        // 创建 CompletableFuture 用于内部执行
        CompletableFuture<FlowResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                FlowLogger.info(new FlowLogContext(context, parallelStep), stepName);
                // 收集所有分支的Future
                CompletableFlowRuntimeExecuteContext
                    predicateContext = new CompletableFlowRuntimeExecuteContext(context, parallelStep);
                CompletableFlowRuntimeExecuteAccess predicateAccess
                    = new CompletableFlowRuntimeExecuteAccess(predicateContext);

                Map<String, StepExecutionHandle<FlowResult>> branchFutures = new HashMap<>();
                parallelStep.getBranches().forEach((targetStepName, branch) -> {
                    FlowRuntimePredicate predicate = branch.flowRuntimePredicate();
                    // 执行分支条件
                    boolean condition = predicate == null || predicate.test(predicateAccess);
                    if (condition) {
                        // 构建新的执行上下文
                        FlowExecutionContext copiedFlowExecutionContext = new FlowExecutionContext(context);
                        CompletableFlowRuntimeExecuteContext branchContext =
                            new CompletableFlowRuntimeExecuteContext(copiedFlowExecutionContext, targetStepName);
                        CompletableFlowRuntimeExecuteAccess branchAccess =
                            new CompletableFlowRuntimeExecuteAccess(branchContext);

                        branchAccess.log("分支执行完成 {}", targetStepName);

                        StepDefinition nextStepDefinition = findStepDefinition(
                            copiedFlowExecutionContext.getFlowDefinition(), branch);

                        StepExecutionHandle<FlowResult> result = taskStepExecutor.execute(nextStepDefinition,
                            copiedFlowExecutionContext);

                        result.getFuture().thenAccept(
                            flowResult -> branchAccess.log("分支执行完成 {}", targetStepName));
                        branchFutures.put(targetStepName, result);
                    }
                });
                // 等待所有分支完成
                CompletableFuture.allOf(branchFutures.values().stream()
                        .map(StepExecutionHandle::getFuture)
                        .toArray(CompletableFuture[]::new))
                    .join();
                // 合并所有分支 context 到主 context
                branchFutures.values().stream()
                    .map(StepExecutionHandle::getResult)
                    .map(FlowResult::getAttributes)
                    .forEach(context.getAttributes()::putAll);

                FlowResult result = FlowResult.success(context);
                callback.onStepCompleted(step, result);
                return result;
            } catch (Exception e) {
                callback.onStepFailed(step, e);
                return new FlowResult(context.getExecutionId(), FlowStatus.ERROR, context.getInputs(), e);
            }
        }, executor);

        // 返回适配后的 StepExecutionHandle
        return new CompletableStepExecutionHandle<>(future);
    }
} 