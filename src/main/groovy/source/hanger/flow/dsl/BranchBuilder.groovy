// src/main/groovy/com/example/flow/dsl/BranchBuilder.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.Branch
import source.hanger.flow.model.EntryNode
import source.hanger.flow.model.TaskNode
import groovy.transform.Internal

// BranchBuilder 处理 branch 关键词内部的逻辑
class BranchBuilder {
    @Internal FlowBuilder flowBuilder // 父构建器引用
    @Internal Branch branch // 当前分支模型

    BranchBuilder(FlowBuilder flowBuilder, Branch branch) {
        this.flowBuilder = flowBuilder
        this.branch = branch
    }

    def name(String text) {
        branch.name = text
    }

    // DSL 关键词: task (在 branch 内部)
    def task(@DelegatesTo(TaskBuilder) Closure<?> closure) {
        def taskNode = new TaskNode()
//        flowBuilder.currentFlow.nodes.put(name, taskNode) // 任务仍然添加到 Flow 的全局节点列表
        branch.startNode = taskNode // 分支的起始节点

        def taskBuilder = new TaskBuilder(flowBuilder, taskNode)
        closure.delegate = taskBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }
}