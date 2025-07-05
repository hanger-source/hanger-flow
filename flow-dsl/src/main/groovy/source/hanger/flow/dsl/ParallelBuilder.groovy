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

/**
 * 并行块DSL构建器
 * 负责解析parallel { ... } DSL块，将Groovy闭包映射为并行模型（ParallelStepDefinition）
 * 支持并行分支、汇合、描述等DSL语法
 */
class ParallelBuilder implements ParallelHint {
    @Internal
    /** 当前正在构建的并行节点模型 */
    private ParallelStepDefinition parallelStepDefinition // 当前并行节点模型

    /**
     * 构造方法，初始化并行节点模型
     * @param parallelStepDefinition 并行节点模型
     */
    ParallelBuilder(ParallelStepDefinition parallelStepDefinition) {
        this.parallelStepDefinition = parallelStepDefinition
    }

    /**
     * DSL关键词：name
     * 设置并行块名称
     */
    void name(String text) {
        parallelStepDefinition.name = text
    }

    /**
     * DSL关键词：description
     * 设置并行块描述信息
     */
    void description(String text) {
        parallelStepDefinition.description = text
    }

    /**
     * DSL关键词：branch
     * 定义并行分支
     * @param text 分支目标任务名称
     * @return BranchBuilder 分支构建器
     */
    BranchBuilder branch(String text) {
        return new BranchBuilder(parallelStepDefinition, text)
    }

    /**
     * DSL关键词：waitFor
     * 定义并行块的汇合点（等待哪些分支完成后继续）
     * @param branchNames 需要等待的分支名称数组
     * @return JoinBuilder 汇合点构建器
     */
    JoinBuilder waitFor(String[] branchNames) {
        parallelStepDefinition.joinBranchNames = branchNames
        return new JoinBuilder(parallelStepDefinition)
    }

    /**
     * DSL关键词：next
     * 定义并行块的汇合后跳转分支
     * @param conditionClosure 条件闭包
     * @return NextBuilder 用于链式指定跳转目标
     */
    NextBuilder next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure) {
        return new NextBuilder(parallelStepDefinition, conditionClosure)
    }

    /**
     * DSL关键词：nextTo
     * 定义并行块的默认跳转目标（无条件）
     * @param nextStepName 跳转目标节点名称
     */
    def nextTo(String nextStepName) {
        next ClosureUtils.TRUE to nextStepName
    }

    /**
     * 分支构建器，支持when条件
     */
    static class BranchBuilder implements BranchHint {
        ParallelStepDefinition parallelStepDefinition
        String nextStepName

        /**
         * 构造方法，初始化分支
         * @param parallelStepDefinition 并行节点模型
         * @param nextStepName 分支目标任务名称
         */
        BranchBuilder(ParallelStepDefinition parallelStepDefinition, String nextStepName) {
            this.parallelStepDefinition = parallelStepDefinition
            this.nextStepName = nextStepName
            when(ClosureUtils.TRUE)
        }

        /**
         * DSL关键词：when
         * 定义分支的条件
         * @param conditionClosure 条件闭包
         */
        void when(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure) {
            parallelStepDefinition.addBranch(new Branch(defaultFlowRuntimePredicate(conditionClosure), nextStepName))
        }
    }
}