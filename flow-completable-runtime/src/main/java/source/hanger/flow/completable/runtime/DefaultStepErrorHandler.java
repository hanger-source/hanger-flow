package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;
import source.hanger.flow.core.runtime.StepErrorHandler;
import source.hanger.flow.core.util.FlowLogger;

/**
 * 默认步骤错误处理策略
 * <p>
 * 提供基础的错误处理逻辑，支持不同类型步骤的差异化处理。
 */
public class DefaultStepErrorHandler implements StepErrorHandler {
    @Override
    public FlowResult handleError(StepDefinition step, FlowExecutionContext context, Exception error) {
        String stepName = step.getName();
        String executionId = context.getExecutionId();
        
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext("ERROR_HANDLER", null, executionId, stepName), "步骤错误处理: " + error.getMessage());
        
        // 根据步骤类型进行差异化处理
        if (step instanceof TaskStepDefinition) {
            return handleTaskStepError((TaskStepDefinition) step, context, error);
        } else {
            return handleGenericStepError(step, context, error);
        }
    }
    
    /**
     * 处理任务步骤错误
     */
    private FlowResult handleTaskStepError(TaskStepDefinition taskStep, FlowExecutionContext context, Exception error) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext("ERROR_HANDLER", null, context.getExecutionId(), taskStep.getName()), "任务步骤错误处理");
        // 可以在这里添加任务特定的错误处理逻辑
        return new FlowResult(context.getExecutionId(), FlowStatus.ERROR, context.getParams(), error);
    }
    
    /**
     * 处理通用步骤错误
     */
    private FlowResult handleGenericStepError(StepDefinition step, FlowExecutionContext context, Exception error) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext("ERROR_HANDLER", null, context.getExecutionId(), step.getName()), "通用步骤错误处理");
        return new FlowResult(context.getExecutionId(), FlowStatus.ERROR, context.getParams(), error);
    }
} 