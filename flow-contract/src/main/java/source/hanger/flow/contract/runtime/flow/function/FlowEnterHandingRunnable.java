package source.hanger.flow.contract.runtime.flow.function;

import source.hanger.flow.contract.runtime.flow.access.FlowEnterHandlingAccess;

/**
 * The interface Condition evaluator.
 */
@FunctionalInterface
public interface FlowEnterHandingRunnable {
    /**
     * enter
     *
     * @param access the access
     */
    void handle(FlowEnterHandlingAccess access);
}