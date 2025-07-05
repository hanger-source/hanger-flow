package source.hanger.flow.dsl

import source.hanger.flow.contract.model.StepDefinition
import source.hanger.flow.dsl.hint.JoinHint
import source.hanger.flow.util.ClosureUtils

/**
 * @author fuhangbo.hanger.uhfun 
 * */
class JoinBuilder implements JoinHint {
    protected NextBuilder nextBuilder
    protected StepDefinition preStepDefinition

    JoinBuilder(StepDefinition preStepDefinition) {
        nextBuilder = new NextBuilder(preStepDefinition, ClosureUtils.TRUE)
        this.preStepDefinition = preStepDefinition
    }

    /*
    * DSL 关键词: to (用于 next/onError 的目标)
    * */

    void nextTo(String nextStepName) {
        nextBuilder.to(nextStepName);
    }
}
