package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic
import source.hanger.flow.contract.runtime.flow.access.FlowEnterHandlingAccess
import source.hanger.flow.contract.runtime.flow.access.FlowErrorHandlingAccess

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * @author fuhangbo.hanger.uhfun 
 * */
@CompileStatic
trait FlowHint {

    abstract void version(String text)

    abstract void name(String text)

    abstract void description(String text)

    abstract void start(String text)

    abstract void onEnter(@DelegatesTo(value = FlowEnterHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> c)

    abstract NextHint onError(@DelegatesTo(value = FlowErrorHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> c)

    abstract void task(@DelegatesTo(value = TaskHint, strategy = DELEGATE_FIRST) Closure<?> c)

    abstract void async(@DelegatesTo(value = AsyncHint, strategy = DELEGATE_FIRST) Closure<?> c)

    abstract void parallel(@DelegatesTo(value = ParallelHint, strategy = DELEGATE_FIRST) Closure<?> c);
}