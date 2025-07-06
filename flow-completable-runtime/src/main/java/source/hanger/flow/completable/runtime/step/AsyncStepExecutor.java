package source.hanger.flow.completable.runtime.step;

import source.hanger.flow.completable.runtime.CompletableStepExecutionHandle;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimeExecuteAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimeExecuteContext;
import source.hanger.flow.contract.model.AsyncStepDefinition;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.status.FlowStatus;
import source.hanger.flow.core.runtime.step.StepExecutionCallback;
import source.hanger.flow.core.runtime.step.StepExecutionHandle;
import source.hanger.flow.core.runtime.step.StepExecutor;
import source.hanger.flow.core.util.FlowLogContext;
import source.hanger.flow.core.util.FlowLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static source.hanger.flow.core.util.FlowStructureUtils.findStepDefinition;

/**
 * 异步节点执行器
 * <p>
 * 专门负责AsyncStepDefinition的执行逻辑。
 * 兼容新的抽象接口，返回 StepExecutionHandle。
 */
public class AsyncStepExecutor implements StepExecutor<Object> {

    private final Executor executor;
    private final TaskStepExecutor taskStepExecutor = new TaskStepExecutor();

    public AsyncStepExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public StepExecutionHandle<Object> execute(StepDefinition step, FlowExecutionContext context,
        StepExecutionCallback<Object> callback) {
        if (!(step instanceof AsyncStepDefinition asyncStep)) {
            throw new IllegalArgumentException("AsyncStepExecutor只支持AsyncStepDefinition类型");
        }

        String stepName = asyncStep.getName();
        // 通知步骤开始
        callback.onStepStarted(step, context.getInputs());

        // 创建 CompletableFuture 用于内部执行
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 异步执行分支，不等待完成
                List<String> branchNames = asyncStep.getBranchNames();
                for (String branchName : branchNames) {
                    // 构建新执行上下文
                    FlowExecutionContext copiedFlowExecutionContext = new FlowExecutionContext(context);
                    CompletableFlowRuntimeExecuteContext branchContext =
                        new CompletableFlowRuntimeExecuteContext(copiedFlowExecutionContext, branchName);
                    CompletableFlowRuntimeExecuteAccess branchAccess =
                        new CompletableFlowRuntimeExecuteAccess(branchContext);

                    CompletableFuture.runAsync(() -> {
                        try {
                            // 记录分支开始
                            branchAccess.log("异步分支开始执行: " + branchName);

                            StepDefinition nextStepDefinition = findStepDefinition(
                                copiedFlowExecutionContext.getFlowDefinition(), branchName);

                            StepExecutionHandle<FlowResult> result = taskStepExecutor.execute(nextStepDefinition,
                                copiedFlowExecutionContext);

                            result.getFuture().thenAccept(
                                flowResult -> branchAccess.log("分支执行完成 {}", branchName));
                        } catch (Exception e) {
                            branchAccess.log("分支执行失败 {}", branchName, e);
                        }
                    }, executor);
                }
                // 立即返回成功结果
                Object result = new FlowResult(context, Map.of());
                callback.onStepCompleted(step, result);
                return result;
            } catch (Exception e) {
                callback.onStepFailed(step, e);
                return new FlowResult(context.getExecutionId(), FlowStatus.ERROR, Map.of());
            }
        }, executor);

        // 返回适配后的 StepExecutionHandle
        return new CompletableStepExecutionHandle<>(future);
    }
} 