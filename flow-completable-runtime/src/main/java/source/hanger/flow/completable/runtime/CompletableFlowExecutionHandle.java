package source.hanger.flow.completable.runtime;

import java.util.concurrent.CompletableFuture;

import source.hanger.flow.core.runtime.engine.FlowEngine;
import source.hanger.flow.core.runtime.engine.FlowExecutionHandle;
import source.hanger.flow.core.runtime.execution.FlowResult;

/**
 * @author fuhangbo.hanger.uhfun
 **/
record CompletableFlowExecutionHandle(String executionId, CompletableFuture<FlowResult> future)
    implements FlowExecutionHandle {

    @Override
    public String executionId() {
        return executionId;
    }

    @Override
    public CompletableFuture<FlowResult> future() {
        return future;
    }

    @Override
    public FlowEngine.FlowExecutionState getStatus() {
        if (future.isDone()) {
            try {
                future.get();
                return FlowEngine.FlowExecutionState.COMPLETED;
            } catch (Exception e) {
                return FlowEngine.FlowExecutionState.FAILED;
            }
        } else if (future.isCancelled()) {
            return FlowEngine.FlowExecutionState.STOPPED;
        } else {
            return FlowEngine.FlowExecutionState.RUNNING;
        }
    }

    @Override
    public boolean stop() {
        return future.cancel(true);
    }
}
