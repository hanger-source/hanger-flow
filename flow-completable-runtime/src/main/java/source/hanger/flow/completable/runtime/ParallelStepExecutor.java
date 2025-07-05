package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.Branch;
import source.hanger.flow.contract.model.ParallelStepDefinition;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicate;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;
import source.hanger.flow.core.runtime.StepExecutor;
import source.hanger.flow.core.util.FlowLogger;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimePredicateAccessContext;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimePredicateAccess;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 并行节点执行器
 * <p>
 * 专门负责ParallelStepDefinition的执行逻辑。
 */
public class ParallelStepExecutor implements StepExecutor {
    @Override
    public CompletableFuture<FlowResult> execute(StepDefinition step, FlowExecutionContext context, Executor executor) {
        if (!(step instanceof ParallelStepDefinition)) {
            throw new IllegalArgumentException("ParallelStepExecutor只支持ParallelStepDefinition类型");
        }
        ParallelStepDefinition parallelStep = (ParallelStepDefinition)step;
        String stepName = parallelStep.getName();
        String executionId = context.getExecutionId();

        FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(
            context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            parallelStep.getName(), null),
            "🔄 并行执行 [" + parallelStep.getBranches().size() + " 个分支]");

        // 收集所有分支的Future
        Map<String, CompletableFuture<FlowResult>> branchFutures = new HashMap<>();
        Map<String, Branch> branches = parallelStep.getBranches();
        for (Map.Entry<String, Branch> entry : branches.entrySet()) {
            String targetStepName = entry.getKey();
            Branch branch = entry.getValue();
            FlowRuntimePredicate predicate = branch.flowRuntimePredicate();
            // 执行分支条件
            boolean condition = false;
            if (predicate != null) {
                CompletableFlowRuntimePredicateAccessContext predicateContext
                    = new CompletableFlowRuntimePredicateAccessContext(context);
                CompletableFlowRuntimePredicateAccess predicateAccess = new CompletableFlowRuntimePredicateAccess(
                    predicateContext);
                condition = predicate.test(predicateAccess);
            } else {
                condition = true;
            }
            FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(
                context.getFlowDefinition().getName(),
                context.getFlowDefinition().getVersion(),
                parallelStep.getName(), targetStepName),
                "🌿 分支 [" + targetStepName + "] " + (condition ? "✅ 条件满足" : "❌ 条件不满足"));

            if (condition) {
                // 这里只负责分支调度，实际分支节点执行需由主引擎调度
                // 这里直接返回已完成的Future作为占位，实际可通过回调主引擎执行分支
                branchFutures.put(targetStepName, CompletableFuture.completedFuture(
                    new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams())));
            }
        }
        // 等待所有分支完成
        return CompletableFuture.allOf(branchFutures.values().toArray(new CompletableFuture[0]))
            .thenApply(v -> new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams()));
    }
} 