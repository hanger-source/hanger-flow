package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * @author fuhangbo.hanger.uhfun
 * */
@CompileStatic
trait ParallelHint {

    abstract void name(String text)

    abstract void description(String text)

    abstract BranchHint branch(String text)

    abstract JoinHint waitFor(String[] branchNames)

    abstract NextHint next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure)

    abstract def nextTo(String nextStepName)
}