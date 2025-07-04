package source.hanger.flow.dsl

import source.hanger.flow.model.Node
import source.hanger.flow.model.Transition

/**
 * @author fuhangbo.hanger.uhfun 
 * */
class NextBuilder {
    private FlowBuilder parentFlowBuilder
    protected Closure<Boolean> condition
    protected Node sourceNode

    NextBuilder(FlowBuilder flowBuilder, Closure<Boolean> booleanClosure, Node sourceNode) {
        this.parentFlowBuilder = flowBuilder
        this.condition = booleanClosure
        this.sourceNode = sourceNode
    }

    // DSL 关键词: to (用于 next/onError 的目标)
    def to(String targetNodeName) {
        def transition = new Transition(condition: condition, targetNodeName: targetNodeName)
        sourceNode.addTransition( transition)
    }
}
