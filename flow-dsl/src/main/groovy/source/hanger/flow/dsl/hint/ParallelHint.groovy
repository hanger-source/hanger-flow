package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * ParallelHint 是 parallel { ... } DSL块的语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 parallel { ... } 块内允许出现的所有关键词和语法结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束并行块的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * parallel {
 *   name '并行处理'
 *   description '支付后并行发货和通知'
 *   branch '物流分配'
 *   branch '拣货打包'
 *   branch '发送邮件' when { context.needEmail }
 *   waitFor '物流分配', '拣货打包' nextTo '订单完成'
 *   next { ... } to '下一步'
 *   nextTo '默认跳转'
 * }
 * </pre>
 * <p>
 * 设计说明：
 *   - 每个abstract方法对应一个DSL关键词，参数类型和注解决定了闭包的委托类型和语法结构
 *   - 便于后续扩展新的并行块DSL语法
 *   - 强制所有parallel DSL实现都必须支持这些语法
 */
@CompileStatic
trait ParallelHint {
    /**
     * DSL关键词：name
     * 设置并行块名称
     * @param text 并行块名称
     */
    abstract void name(String text)

    /**
     * DSL关键词：description
     * 设置并行块描述信息
     * @param text 并行块描述
     */
    abstract void description(String text)

    /**
     * DSL关键词：branch
     * 定义并行分支
     * @param text 分支目标任务名称
     * @return BranchHint 分支构建器
     */
    abstract BranchHint branch(String text)

    /**
     * DSL关键词：waitFor
     * 定义并行块的汇合点（等待哪些分支完成后继续）
     * @param branchNames 需要等待的分支名称数组
     * @return JoinHint 汇合点构建器
     */
    abstract JoinHint waitFor(String[] branchNames)

    /**
     * DSL关键词：next
     * 定义并行块的汇合后跳转分支
     * @param conditionClosure 条件闭包，委托为FlowRuntimePredicateAccess
     * @return NextHint 用于链式指定跳转目标
     */
    abstract NextHint next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure)

    /**
     * DSL关键词：nextTo
     * 定义并行块的默认跳转目标（无条件）
     * @param nextStepName 跳转目标节点名称
     */
    abstract def nextTo(String nextStepName)
}