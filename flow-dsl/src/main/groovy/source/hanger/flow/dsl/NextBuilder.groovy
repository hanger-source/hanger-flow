package source.hanger.flow.dsl

import source.hanger.flow.contract.model.StepDefinition
import source.hanger.flow.contract.model.Transition
import source.hanger.flow.dsl.hint.NextHint

import static source.hanger.flow.util.ClosureUtils.defaultFlowRuntimePredicate

/**
 * @author fuhangbo.hanger.uhfun 
 * */
class NextBuilder implements NextHint {
    protected Closure<?> booleanClosure
    protected StepDefinition preStepDefinition

    NextBuilder(StepDefinition preStepDefinition, Closure<?> booleanClosure) {
        this.booleanClosure = booleanClosure
        this.preStepDefinition = preStepDefinition
    }

    /*
    * DSL 关键词: to (用于 next/onError 的目标)
    * */

    void to(String nextStepName) {
        // 适配闭包的执行逻辑为java的实现
        def transition = new Transition(defaultFlowRuntimePredicate(booleanClosure), nextStepName)
        preStepDefinition.addTransition(transition)
    }
}
