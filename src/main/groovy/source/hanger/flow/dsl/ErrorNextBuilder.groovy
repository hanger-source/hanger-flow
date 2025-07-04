package source.hanger.flow.dsl

import source.hanger.flow.model.Node
import source.hanger.flow.model.Transition

/**
 * @author fuhangbo.hanger.uhfun 
 * */
class ErrorNextBuilder extends NextBuilder {
    ErrorNextBuilder(FlowBuilder parentFlowBuilder, Closure<?> closure, Node node) {
        super(parentFlowBuilder, { true } as Closure<Boolean>, node as Node) // onError 通常是无条件的跳转
        // 将错误处理逻辑存储在 errorTransition 中
//        sourceNode.errorTransition = new Transition(condition: {true}, targetNodeName: "") // 预设一个空的
//            taskNode.errorTransition.errorHandlingLogic = closure // 实际错误处理逻辑
    }

    @Override // 覆盖 to 方法以设置错误处理的目标
    def to(String targetNodeName) {
//        sourceNode.errorTransition.targetNodeName = targetNodeName
        println "  ${sourceNode.name} onError set to: ${targetNodeName}"
    }
}