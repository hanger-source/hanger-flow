package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess
import source.hanger.flow.contract.runtime.task.access.FlowTaskEnterHandingAccess
import source.hanger.flow.contract.runtime.task.access.FlowTaskErrorHandlingAccess
import source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * @author fuhangbo.hanger.uhfun
 * */
@CompileStatic
trait TaskHint {

    abstract void name(String text)

    abstract void description(String text)

    abstract void onEnter(@DelegatesTo(value = FlowTaskEnterHandingAccess, strategy = DELEGATE_FIRST) Closure<?> c)

    abstract void run(@DelegatesTo(value = FlowTaskRunAccess, strategy = DELEGATE_FIRST) Closure<?> runClosure)

    abstract NextHint next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure)

    abstract def nextTo(String nextStepName);

    abstract NextHint onError(@DelegatesTo(value = FlowTaskErrorHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> c)
}