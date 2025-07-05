package source.hanger.flow.contract.runtime.task.function;

import source.hanger.flow.contract.runtime.task.access.FlowTaskEnterHandingAccess;

/**
 * The interface Condition evaluator.
 */
@FunctionalInterface
public interface FlowTaskEnterHandingRunnable {
    /**
     * handle
     *
     * @param access the access
     */
    void handle(FlowTaskEnterHandingAccess access);
}