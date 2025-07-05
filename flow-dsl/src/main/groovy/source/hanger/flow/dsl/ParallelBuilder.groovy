package source.hanger.flow.dsl

import groovy.transform.Internal
import source.hanger.flow.contract.model.Branch
import source.hanger.flow.contract.model.ParallelStepDefinition
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess
import source.hanger.flow.dsl.hint.BranchHint
import source.hanger.flow.dsl.hint.ParallelHint
import source.hanger.flow.util.ClosureUtils

import static groovy.lang.Closure.DELEGATE_FIRST
import static source.hanger.flow.util.ClosureUtils.defaultFlowRuntimePredicate

// ParallelBuilder 处理 parallel 关键词内部的逻辑
class ParallelBuilder implements ParallelHint {
    @Internal
    private ParallelStepDefinition parallelStepDefinition // 当前并行节点模型

    ParallelBuilder(ParallelStepDefinition parallelStepDefinition) {
        this.parallelStepDefinition = parallelStepDefinition
    }

    void name(String text) {
        parallelStepDefinition.name = text
    }

    // DSL 关键词: description (在 parallel 内部)
    void description(String text) {
        parallelStepDefinition.description = text
    }

    BranchBuilder branch(String text) {
        return new BranchBuilder(parallelStepDefinition, text)
    }

    JoinBuilder waitFor(String[] branchNames) {
        parallelStepDefinition.joinBranchNames = branchNames
        return new JoinBuilder(parallelStepDefinition)
    }

    // DSL 关键词: next (在 parallel 内部，用于汇聚)
    // Parallel 块的 next 是特殊汇聚点
    NextBuilder next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure) {
        return new NextBuilder(parallelStepDefinition, conditionClosure)
    }

    def nextTo(String nextStepName) {
        next ClosureUtils.TRUE to nextStepName
    }

    static class BranchBuilder implements BranchHint {
        ParallelStepDefinition parallelStepDefinition
        String nextStepName

        BranchBuilder(ParallelStepDefinition parallelStepDefinition, String nextStepName) {
            this.parallelStepDefinition = parallelStepDefinition
            this.nextStepName = nextStepName
            when(ClosureUtils.TRUE)
        }

        void when(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure) {
            parallelStepDefinition.addBranch(new Branch(defaultFlowRuntimePredicate(conditionClosure), nextStepName))
        }
    }
}