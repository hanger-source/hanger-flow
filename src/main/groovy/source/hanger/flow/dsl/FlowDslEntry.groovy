// src/main/groovy/com/example/flow/dsl/FlowDslEntry.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.FlowDefinition

// FlowDslEntry 是你的 DSL 的主要入口点
// 它通常包含一个全局的方法，用于开始定义流程
class FlowDslEntry {

    // 这个方法是 DSL 的入口关键词 'flow'
    // 它会创建一个 FlowBuilder 实例，并将其作为闭包的委托
    // 这样，在 flow {} 内部就可以直接调用 FlowBuilder 的方法
    static String END = "结束"

    static FlowDefinition flow(@DelegatesTo(FlowBuilder) Closure<?> closure) {
        def builder = new FlowBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call() // 执行 DSL 闭包，开始构建流程
        return builder.currentFlow // 返回构建好的 FlowDefinition 对象
    }
}