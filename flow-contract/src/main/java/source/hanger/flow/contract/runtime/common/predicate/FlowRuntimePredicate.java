package source.hanger.flow.contract.runtime.common.predicate;

/**
 * The interface Condition evaluator.
 */
@FunctionalInterface
public interface FlowRuntimePredicate {
    /**
     * Evaluate boolean.
     *
     * @param access the access
     * @return the boolean
     */
    boolean test(FlowRuntimePredicateAccess access);
}