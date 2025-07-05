package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;
import source.hanger.flow.core.runtime.StepExecutor;
import source.hanger.flow.core.util.FlowLogger;
import source.hanger.flow.completable.runtime.context.CompletableFlowTaskRunAccessContext;
import source.hanger.flow.completable.runtime.access.CompletableFlowTaskRunAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowTaskEnterHandingAccessContext;
import source.hanger.flow.completable.runtime.context.CompletableFlowTaskErrorHandingAccessContext;
import source.hanger.flow.completable.runtime.access.CompletableFlowTaskEnterHandingAccess;
import source.hanger.flow.completable.runtime.access.CompletableFlowTaskErrorHandlingAccess;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 任务节点执行器
 * <p>
 * 专门负责TaskStepDefinition的执行逻辑。
 */
public class TaskStepExecutor implements StepExecutor {
    @Override
    public CompletableFuture<FlowResult> execute(StepDefinition step, FlowExecutionContext context, Executor executor) {
        if (!(step instanceof TaskStepDefinition)) {
            throw new IllegalArgumentException("TaskStepExecutor只支持TaskStepDefinition类型");
        }
        TaskStepDefinition taskStep = (TaskStepDefinition) step;
        String stepName = taskStep.getName();
        String executionId = context.getExecutionId();
        
        FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(
            context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            stepName, null),
            "⚡ 任务执行: 开始执行任务逻辑");

        return CompletableFuture.supplyAsync(() -> {
            // 执行任务进入回调（onEnter）
            if (taskStep.getEnterHandingRunnable() != null) {
                var accessContext = new CompletableFlowTaskEnterHandingAccessContext(context, stepName);
                var access = new CompletableFlowTaskEnterHandingAccess(accessContext);
                taskStep.getEnterHandingRunnable().handle(access);
            }
            try {
                // 执行任务主体
                FlowTaskRunnable taskRunnable = taskStep.getTaskRunnable();
                if (taskRunnable != null) {
                    CompletableFlowTaskRunAccessContext accessContext = new CompletableFlowTaskRunAccessContext(context, stepName);
                    CompletableFlowTaskRunAccess access = new CompletableFlowTaskRunAccess(accessContext);
                    taskRunnable.run(access);
                }
                return new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams());
            } catch (Exception e) {
                // 执行任务错误回调（onError）
                if (taskStep.getErrorHandingRunnable() != null) {
                    var accessContext = new CompletableFlowTaskErrorHandingAccessContext(context, stepName, e);
                    var access = new CompletableFlowTaskErrorHandlingAccess(accessContext);
                    taskStep.getErrorHandingRunnable().handle(access);
                }
                return new FlowResult(context.getExecutionId(), FlowStatus.ERROR, context.getParams(), e);
            }
        }, executor);
    }
} 