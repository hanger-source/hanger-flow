package source.hanger.flow.dsl

import source.hanger.flow.contract.model.StepDefinition
import source.hanger.flow.dsl.hint.JoinHint
import source.hanger.flow.util.ClosureUtils

/**
 * 并行汇合点DSL构建器
 * 负责解析waitFor ... nextTo ...等DSL语法，将汇合点映射为流程模型的跳转
 * 支持链式指定汇合后的跳转目标
 */
class JoinBuilder implements JoinHint {
    /** 内部持有的NextBuilder */
    protected NextBuilder nextBuilder
    /** 前置步骤定义 */
    protected StepDefinition preStepDefinition

    /**
     * 构造方法，初始化汇合点
     * @param preStepDefinition 前置步骤定义
     */
    JoinBuilder(StepDefinition preStepDefinition) {
        nextBuilder = new NextBuilder(preStepDefinition, ClosureUtils.TRUE)
        this.preStepDefinition = preStepDefinition
    }

    /*
    * DSL 关键词: to (用于 next/onError 的目标)
    * */

    /**
     * DSL关键词：nextTo
     * 指定汇合点的跳转目标
     * @param nextStepName 跳转目标节点名称
     */
    void nextTo(String nextStepName) {
        nextBuilder.to(nextStepName);
    }
}
