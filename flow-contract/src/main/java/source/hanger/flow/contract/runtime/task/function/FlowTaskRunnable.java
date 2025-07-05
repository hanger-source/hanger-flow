package source.hanger.flow.contract.runtime.task.function;

import source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess;

/**
 * The interface Condition evaluator.
 */
@FunctionalInterface
public interface FlowTaskRunnable {
    /**
     * Run.
     *
     * @param access the access
     */
    void run(FlowTaskRunAccess access);
}