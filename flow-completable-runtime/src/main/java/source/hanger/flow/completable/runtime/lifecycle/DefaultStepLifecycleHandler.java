package source.hanger.flow.completable.runtime.lifecycle;

import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.lifecycle.StepLifecycleHandler;
import source.hanger.flow.core.util.FlowLogContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * 默认步骤生命周期处理器
 * <p>
 * 提供基础的步骤生命周期处理逻辑。
 */
public class DefaultStepLifecycleHandler implements StepLifecycleHandler {
    @Override
    public void onStepStart(StepDefinition step, FlowExecutionContext context) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogContext(
            context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            context.getExecutionId(),
            step.getName()),
            "步骤生命周期: 开始执行");
    }

    @Override
    public void onStepComplete(StepDefinition step, FlowExecutionContext context) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogContext(
            context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            context.getExecutionId(),
            step.getName()),
            "步骤生命周期: 执行完成");
    }

    @Override
    public void onStepError(StepDefinition step, FlowExecutionContext context, Exception error) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogContext(
            context.getFlowDefinition().getName(),
            context.getFlowDefinition().getVersion(),
            context.getExecutionId(),
            step.getName()),
            "步骤生命周期: 执行错误 - " + error.getMessage());
    }
} 