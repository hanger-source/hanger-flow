package source.hanger.flow.completable.runtime.lifecycle;

import source.hanger.flow.completable.runtime.access.CompletableFlowEnterHandlingAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowEnterHandingAccessContext;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.runtime.FlowLifecycleHandler;
import source.hanger.flow.core.util.FlowLogger;

/**
 * 默认流程生命周期处理器
 * <p>
 * 提供基础的流程生命周期处理逻辑。
 */
public class DefaultFlowLifecycleHandler implements FlowLifecycleHandler {
    @Override
    public void onFlowStart(FlowDefinition flowDefinition, FlowExecutionContext context) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(flowDefinition.getName(), flowDefinition.getVersion(), context.getExecutionId(), null), "流程生命周期: 开始执行");
        // 调用DSL定义的流程进入回调
        if (flowDefinition.getEnterHandingRunnable() != null) {
            var accessContext = new CompletableFlowEnterHandingAccessContext(context);
            var access = new CompletableFlowEnterHandlingAccess(accessContext);
            flowDefinition.getEnterHandingRunnable().handle(access);
        }
    }

    @Override
    public void onFlowComplete(FlowDefinition flowDefinition, FlowExecutionContext context) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(flowDefinition.getName(), flowDefinition.getVersion(), context.getExecutionId(), null), "流程生命周期: 执行完成");
    }

    @Override
    public void onFlowError(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error) {
        FlowLogger.log(FlowLogger.Level.DEBUG, new FlowLogger.FlowLogContext(flowDefinition.getName(), flowDefinition.getVersion(), context.getExecutionId(), null), "流程生命周期: 执行错误 - " + error.getMessage());
        // 调用DSL定义的流程错误回调
        if (flowDefinition.getErrorHandingRunnable() != null) {
            var accessContext
                = new source.hanger.flow.completable.runtime.context.CompletableFlowErrorHandingAccessContext(context,
                error);
            var access = new source.hanger.flow.completable.runtime.access.CompletableFlowErrorHandlingAccess(
                accessContext);
            flowDefinition.getErrorHandingRunnable().handle(access);
        }
    }
} 