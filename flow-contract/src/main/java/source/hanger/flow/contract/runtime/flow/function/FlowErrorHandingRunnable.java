package source.hanger.flow.contract.runtime.flow.function;

import source.hanger.flow.contract.runtime.flow.access.FlowErrorHandlingAccess;

/**
 * The interface Condition evaluator.
 */
@FunctionalInterface
public interface FlowErrorHandingRunnable {
    /**
     * @param access the access
     */
    void handle(FlowErrorHandlingAccess access);
}