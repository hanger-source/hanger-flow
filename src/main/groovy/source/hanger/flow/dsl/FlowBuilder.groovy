// src/main/groovy/com/example/flow/dsl/FlowBuilder.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.*
import groovy.transform.Internal

// FlowBuilder 是 Flow DSL 的入口和顶级构建器
class FlowBuilder {
    @Internal // 标记为内部属性，不应该在 DSL 中直接访问
    FlowDefinition currentFlow // 当前正在构建的流程定义

    @Internal
    Node currentContextNode // 当前 DSL 块所在的节点（task, entry, parallel）

    // 用于在 run/onError 闭包中传递上下文和异常
    final def context = [:]
    Throwable exception = null

    FlowBuilder(String description = null) {
        currentFlow = new FlowDefinition(description: description)
    }

    def name(String text) {
        currentFlow.name = text
    }

    // DSL 关键词: description (在 flow 内部)
    def description(String text) {
        currentFlow.description = text
    }

    // DSL 关键词: onEnter
    def onEnter(@DelegatesTo(value = FlowBuilder, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
        currentFlow.onEnterLogic = closure
        closure.call()
    }

    // DSL 关键词: task
    // TaskBuilder 作为闭包的 delegate
    def task(@DelegatesTo(TaskBuilder) Closure<?> closure) {
        def taskNode = new TaskNode()

        // 如果是流程的第一个任务，则设置为起始节点
        if (currentFlow.startNode == null) {
            currentFlow.startNode = taskNode
        }

        def taskBuilder = new TaskBuilder(this, taskNode) // 传入 FlowBuilder 引用和当前任务节点
        closure.delegate = taskBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST // 优先委托对象

        currentContextNode = taskNode // 更新当前上下文节点

        closure.call()
    }

    // DSL 关键词: entry
    // EntryBuilder 作为闭包的 delegate
    def entry(@DelegatesTo(EntryBuilder) Closure<?> closure) {
        def entryNode = new EntryNode()

        def entryBuilder = new EntryBuilder(this, entryNode) // 传入 FlowBuilder 引用和当前入口节点
        closure.delegate = entryBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        currentContextNode = entryNode // 更新当前上下文节点

        closure.call()
    }

    // DSL 关键词: parallel
    // ParallelBuilder 作为闭包的 delegate
    def parallel(@DelegatesTo(ParallelBuilder) Closure<?> closure) {
        def parallelNode = new ParallelNode()

        // 如果是流程的第一个任务/节点，则设置为起始节点
        if (currentFlow.startNode == null) {
            currentFlow.startNode = parallelNode
        }

        def parallelBuilder = new ParallelBuilder(this, parallelNode) // 传入 FlowBuilder 引用和并行节点
        closure.delegate = parallelBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST

        currentContextNode = parallelNode // 更新当前上下文节点

        closure.call()
    }

    // DSL 关键词: agent (作为顶层定义)
    def agent(@DelegatesTo(AgentBuilder) Closure<?> closure = null) {
        def agentDef = new AgentDefinition()
        // FlowDefinition 中可以维护一个 agent 列表
        // currentFlow.agents.put(name, agentDef) // 需要在 FlowDefinition 中添加 agents 属性

        if (closure) {
            def agentBuilder = new AgentBuilder(agentDef)
            closure.delegate = agentBuilder
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
        }
        println "Agent '$agentDef.name' defined: ${agentDef}" // 模拟打印 agent 定义
    }

    // 用于在 task/entry/parallel 内部，链接到 FlowBuilder 的下一个方法
    // 这允许在 task 闭包内部直接调用 FlowBuilder 的 next()
    def methodMissing(String name, args) {
        if (["task", "entry", "parallel", "agent"].contains(name) || name.startsWith("next")) {
            // 如果方法是 DSL 关键词，且当前没有对应的处理，
            // 尝试将它转发给 FlowBuilder
            // 这里为了简化，直接判断关键词，实际需要更严谨的委托链
            if (currentFlow.metaClass.hasMethod(this, name, args as Object[])) {
                return currentFlow.metaClass.invokeMethod(this, name, args)
            }
        }
        throw new MissingMethodException(name, currentFlow.getClass(), args)
    }
}