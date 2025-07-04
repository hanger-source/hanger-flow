// src/main/groovy/com/example/flow/dsl/ParallelBuilder.groovy
package source.hanger.flow.dsl

import source.hanger.flow.model.Branch
import source.hanger.flow.model.ParallelNode
import source.hanger.flow.model.Transition
import groovy.transform.Internal

// ParallelBuilder 处理 parallel 关键词内部的逻辑
class ParallelBuilder {
    @Internal FlowBuilder flowBuilder // 父构建器引用
    @Internal ParallelNode parallelNode // 当前并行节点模型

    ParallelBuilder(FlowBuilder flowBuilder, ParallelNode parallelNode) {
        this.flowBuilder = flowBuilder
        this.parallelNode = parallelNode
    }

    // DSL 关键词: description (在 parallel 内部)
    def description(String text) {
        parallelNode.description = text
    }

    // DSL 关键词: branch
    // BranchBuilder 作为闭包的 delegate
    def branch(@DelegatesTo(BranchBuilder) Closure<?> closure) {
        def branch = new Branch()
//        parallelNode.branches.put(name, branch)

        def branchBuilder = new BranchBuilder(flowBuilder, branch) // 传入 FlowBuilder 和当前分支
        closure.delegate = branchBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }

    // DSL 关键词: next (在 parallel 内部，用于汇聚)
    // Parallel 块的 next 是特殊汇聚点
    NextBuilder next(@DelegatesTo(value = ParallelBuilder, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> condition) {
        return new ParallelNextBuilder(flowBuilder, condition, parallelNode)
    }

    @Internal
    static class ParallelNextBuilder extends NextBuilder {
        ParallelNextBuilder(FlowBuilder parentFlowBuilder, Closure<Boolean> condition, ParallelNode parallelNode) {
            super(parentFlowBuilder, condition, parallelNode)
        }

        @Override
        def to(String targetNodeName) {
            ((ParallelNode)sourceNode).mergeTransition = new Transition(condition: condition, targetNodeName: targetNodeName)
            println "  ${sourceNode.name} parallel merge to: ${targetNodeName}"
        }
    }
}