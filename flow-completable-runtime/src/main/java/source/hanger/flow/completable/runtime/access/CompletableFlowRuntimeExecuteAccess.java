package source.hanger.flow.completable.runtime.access;

import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteContext;
import source.hanger.flow.core.runtime.step.StepExecutionCallback;
import source.hanger.flow.core.util.FlowLogContext;
import source.hanger.flow.core.util.FlowLogger;

/**
 * CompletableFuture统一执行访问接口实现
 */
public class CompletableFlowRuntimeExecuteAccess implements FlowRuntimeExecuteAccess {
    private final FlowRuntimeExecuteContext context;
    private final StepExecutionCallback<?> callback;
    private final FlowStreamingChannel channel;

    public CompletableFlowRuntimeExecuteAccess(FlowRuntimeExecuteContext context) {
        this.context = context;
        callback = null;
        channel = context.getChannel();
    }

    public CompletableFlowRuntimeExecuteAccess(FlowRuntimeExecuteContext context, StepExecutionCallback<?> callback) {
        this.context = context;
        this.callback = callback;
        channel = context.getChannel();
    }

    @Override
    public FlowRuntimeExecuteContext getContext() {
        return context;
    }

    @Override
    public void log(String message, Object... args) {
        FlowLogger.info(new FlowLogContext(
            context.getFlowName(),
            context.getVersion(),
            context.getExecutionId(),
            context.getStepName()), args);
    }

    /**
     * 获取channel对象，用于DSL中的channel访问
     * 这是为了支持DSL中的 channel.emit() 语法
     */
    public FlowStreamingChannel getChannel() {
        return channel;
    }
}