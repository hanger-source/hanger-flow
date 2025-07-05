package source.hanger.flow.contract.runtime.task.function;

import source.hanger.flow.contract.runtime.task.access.FlowTaskErrorHandlingAccess;

/**
 * The interface Condition evaluator.
 */
@FunctionalInterface
public interface FlowTaskErrorHandingRunnable {
    /**
     * Run.
     *
     * @param access the access
     */
    void handle(FlowTaskErrorHandlingAccess access);
}