package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * @author fuhangbo.hanger.uhfun 
 * */
@CompileStatic
trait BranchHint {
    abstract void when(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure)
}