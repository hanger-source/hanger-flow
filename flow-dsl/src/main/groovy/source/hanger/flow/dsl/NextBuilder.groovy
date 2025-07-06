package source.hanger.flow.dsl

import source.hanger.flow.contract.model.StepDefinition
import source.hanger.flow.contract.model.Transition
import source.hanger.flow.dsl.hint.NextHint

import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess
import source.hanger.flow.util.ClosureUtils

import static source.hanger.flow.util.ClosureUtils.*

/**
 * 条件跳转DSL构建器
 * 负责解析next { ... } to '目标'等DSL语法，将条件闭包映射为Transition模型
 * 支持任务、并行、异步等节点的条件跳转
 */
class NextBuilder implements NextHint {
    /** 条件闭包 */
    protected Closure<?> booleanClosure
    /** 前置步骤定义 */
    protected StepDefinition preStepDefinition

    /**
     * 构造方法，初始化条件跳转
     * @param preStepDefinition 前置步骤定义
     * @param booleanClosure 条件闭包
     */
    NextBuilder(StepDefinition preStepDefinition, Closure<?> booleanClosure) {
        this.booleanClosure = booleanClosure
        this.preStepDefinition = preStepDefinition
    }

    /**
     * DSL关键词：to
     * 指定条件跳转的目标节点
     * @param nextStepName 跳转目标节点名称
     */
    void to(String nextStepName) {
        // 适配闭包的执行逻辑为java的实现
        def transition = new Transition(toFlowRuntimePredicate(booleanClosure), nextStepName)
        preStepDefinition.addTransition(transition)
    }
}
