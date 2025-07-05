package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.AsyncStepDefinition;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;
import source.hanger.flow.core.runtime.StepExecutor;
import source.hanger.flow.core.util.FlowLogger;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimePredicateAccessContext;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimePredicateAccess;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 异步节点执行器
 * <p>
 * 专门负责AsyncStepDefinition的执行逻辑。
 */
public class AsyncStepExecutor implements StepExecutor {
    @Override
    public CompletableFuture<FlowResult> execute(StepDefinition step, FlowExecutionContext context, Executor executor) {
        if (!(step instanceof AsyncStepDefinition)) {
            throw new IllegalArgumentException("AsyncStepExecutor只支持AsyncStepDefinition类型");
        }
        AsyncStepDefinition asyncStep = (AsyncStepDefinition) step;
        String stepName = asyncStep.getName();
        String executionId = context.getExecutionId();
        
        // FlowLogger.log(FlowLogger.Level.INFO, ctx, "⚡ 异步执行 [" + asyncStep.getBranchNames().size() + " 个分支]");

        // 异步执行分支，不等待完成
        List<String> branchNames = asyncStep.getBranchNames();
        for (String branchName : branchNames) {
            CompletableFuture.runAsync(() -> {
                // 实际分支节点执行需由主引擎调度，这里仅做占位
                CompletableFlowRuntimePredicateAccessContext predicateContext = new CompletableFlowRuntimePredicateAccessContext(context);
                CompletableFlowRuntimePredicateAccess predicateAccess = new CompletableFlowRuntimePredicateAccess(predicateContext);
                FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
                    context.getFlowDefinition().getName(),
                    context.getFlowDefinition().getVersion(),
                    "ASYNC_BRANCH", stepName),
                    "异步分支执行: " + branchName);
            }, executor);
        }
        return CompletableFuture.completedFuture(new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams()));
    }
} 