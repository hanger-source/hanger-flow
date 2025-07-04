// src/main/groovy/com/example/flow/dsl/EntryBuilder.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.EntryNode
import source.hanger.flow.model.TaskNode
import groovy.transform.Internal

// EntryBuilder 处理 entry 关键词内部的逻辑
class EntryBuilder {
    @Internal FlowBuilder flowBuilder // 父构建器引用
    @Internal EntryNode entryNode // 当前入口节点模型

    EntryBuilder(FlowBuilder flowBuilder, EntryNode entryNode) {
        this.flowBuilder = flowBuilder
        this.entryNode = entryNode
    }

    def name(String text) {
        entryNode.name = text
    }

    // DSL 关键词: description (在 entry 内部)
    def description(String text) {
        entryNode.description = text
    }

    // DSL 关键词: task (在 entry 内部)
    // Entry 内部可以定义任务，通常是它的第一个任务
    def task(@DelegatesTo(TaskBuilder) Closure<?> closure) {
        def taskNode = new TaskNode()
//        flowBuilder.currentFlow.nodes.put(name, taskNode) // 任务仍然添加到 Flow 的全局节点列表
        entryNode.entryStartTask = taskNode // 假设 entry 只有一个起始任务

        def taskBuilder = new TaskBuilder(flowBuilder, taskNode)
        closure.delegate = taskBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }

    // 在 Entry 内部也可以直接定义 next 到其他节点
    NextBuilder next(@DelegatesTo(value = EntryBuilder, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> condition) {
        // 这里的 next 实际上应该链接到 entryNode 内部逻辑的下一个步骤
        // 为了简化，这里让它链接到 FlowBuilder 的上下文，假设 entry 内部只有一个 task 或直接跳转
        return new NextBuilder(flowBuilder, condition, entryNode) // 复用 TaskBuilder 的 NextBuilder
    }

    // 在 Entry 内部也可以直接定义 next 到其他节点
    NextBuilder nextTo(String targetNodeName) {
        // 这里的 next 实际上应该链接到 entryNode 内部逻辑的下一个步骤
        // 为了简化，这里让它链接到 FlowBuilder 的上下文，假设 entry 内部只有一个 task 或直接跳转
        return new NextBuilder(flowBuilder, {true} as Closure<Boolean>, entryNode).to(targetNodeName) // 复用 TaskBuilder 的 NextBuilder
    }


}