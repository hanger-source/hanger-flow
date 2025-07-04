// src/main/groovy/com/example/flow/dsl/TaskBuilder.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.TaskNode
import groovy.transform.Internal

// TaskBuilder 处理 task 关键词内部的逻辑
class TaskBuilder {
    @Internal FlowBuilder flowBuilder // 父构建器引用
    @Internal TaskNode taskNode // 当前任务节点模型

    TaskBuilder(FlowBuilder flowBuilder, TaskNode taskNode) {
        this.flowBuilder = flowBuilder
        this.taskNode = taskNode
    }

    def name(String text) {
        taskNode.name = text
    }

    // DSL 关键词: description (在 task 内部)
    def description(String text) {
        taskNode.description = text
    }

    // DSL 关键词: run
    def run(@DelegatesTo(value = TaskBuilder, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
        taskNode.runLogic = closure
    }

    // DSL 关键词: next (在 task 内部)
    NextBuilder next(@DelegatesTo(value = TaskBuilder, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> condition) {
        return new NextBuilder(flowBuilder, condition, taskNode) // next 的目标是 FlowBuilder 管理的节点
    }

    NextBuilder nextTo(String targetNodeName) {
        // 这里的 next 实际上应该链接到 entryNode 内部逻辑的下一个步骤
        // 为了简化，这里让它链接到 FlowBuilder 的上下文，假设 entry 内部只有一个 task 或直接跳转
        return new NextBuilder(flowBuilder, {true} as Closure<Boolean>, taskNode).to(targetNodeName) // 复用 TaskBuilder 的 NextBuilder
    }

    // DSL 关键词: onError (在 task 内部)
    ErrorNextBuilder onError(@DelegatesTo(value = TaskBuilder, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
        // 创建一个临时的 ErrorNextBuilder 来处理 .to()
        return new ErrorNextBuilder(flowBuilder, closure, taskNode)
    }
}