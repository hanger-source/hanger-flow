package source.hanger.flow.util

import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicate
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess

/**
 * @author fuhangbo.hanger.uhfun
 * */
class ClosureUtils {

    public static final Closure<?> TRUE = { true }

    static FlowRuntimePredicate defaultFlowRuntimePredicate(Closure<?> booleanClosure) {
        return new FlowRuntimePredicate() {
            @Override
            boolean test(FlowRuntimePredicateAccess access) {
                booleanClosure.setDelegate(access)
                booleanClosure.setResolveStrategy(Closure.DELEGATE_FIRST)
                def result = booleanClosure.call()
                if (!(result instanceof Boolean)) {
                    throw new IllegalArgumentException("Condition closure must return a boolean value, but returned: ${result?.getClass()?.name} (${result})")
                }
                return (boolean) result
            }
        }
    }
}
