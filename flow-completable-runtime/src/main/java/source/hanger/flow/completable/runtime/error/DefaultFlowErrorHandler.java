package source.hanger.flow.completable.runtime.error;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.error.FlowErrorHandler;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.status.FlowStatus;
import source.hanger.flow.core.util.FlowLogContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * 默认流程错误处理策略
 * <p>
 * 提供基础的流程级别错误处理逻辑。
 */
public class DefaultFlowErrorHandler implements FlowErrorHandler {
    @Override
    public FlowResult handleError(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error) {
        String flowName = flowDefinition.getName();
        String executionId = context.getExecutionId();

        FlowLogger.log(FlowLogger.Level.DEBUG,
            new FlowLogContext(flowName, flowDefinition.getVersion(), executionId, null),
            "流程错误处理: " + error.getMessage());
        
        // 执行流程错误处理回调
        executeFlowErrorCallback(flowDefinition, context, error);

        return new FlowResult(context.getExecutionId(), FlowStatus.ERROR, context.getInputs(), error);
    }
    
    /**
     * 执行流程错误处理回调
     */
    private void executeFlowErrorCallback(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error) {
        try {
            // 这里可以调用流程定义中的错误处理回调
            FlowLogger.log(FlowLogger.Level.DEBUG,
                new FlowLogContext(flowDefinition.getName(), flowDefinition.getVersion(), context.getExecutionId(),
                    null), "流程错误处理回调执行: " + error.getMessage());
        } catch (Exception e) {
            FlowLogger.log(FlowLogger.Level.DEBUG,
                new FlowLogContext(flowDefinition.getName(), flowDefinition.getVersion(), context.getExecutionId(),
                    null), "流程错误处理回调异常: " + e.getMessage());
        }
    }
} 