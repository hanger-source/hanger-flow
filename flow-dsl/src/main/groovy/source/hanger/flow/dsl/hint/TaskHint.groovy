package source.hanger.flow.dsl.hint

import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess
import source.hanger.flow.contract.runtime.common.FlowClosure
import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * TaskHint 是 task { ... } DSL块的语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 task { ... } 块内允许出现的所有关键词和语法结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束任务节点的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * task {
 *   name '初始化'
 *   description '准备订单数据'
 *   onEnter { ... }
 *   run { ... }
 *   next { context.flag } to '下一步'
 *   nextTo '直接跳转'
 *   onError { ... } to '错误处理'
 * }
 * </pre>
 * <p>
 * 设计说明：
 *   - 每个abstract方法对应一个DSL关键词，参数类型和注解决定了闭包的委托类型和语法结构
 *   - 便于后续扩展新的任务级DSL语法
 *   - 强制所有task DSL实现都必须支持这些语法
 */
trait TaskHint {
    /**
     * DSL关键词：name
     * 设置任务名称
     * @param text 任务名称
     */
    abstract void name(String text)

    /**
     * DSL关键词：description
     * 设置任务描述信息
     * @param text 任务描述
     */
    abstract void description(String text)

    /**
     * DSL关键词：onEnter
     * 定义任务进入时的处理逻辑
     * @param c 处理闭包，委托为FlowTaskEnterHandingAccess
     */
    abstract void onEnter(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> c)

    /**
     * DSL关键词：run
     * 定义任务的核心执行逻辑
     * @param runClosure 执行闭包，委托为FlowTaskRunAccess
     */
    abstract void run(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> runClosure)

    /**
     * DSL关键词：next
     * 定义任务的条件跳转分支
     * @param conditionClosure 条件闭包，委托为FlowRuntimePredicateAccess
     * @return NextHint 用于链式指定跳转目标
     */
    abstract NextHint next(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure)

    /**
     * DSL关键词：nextTo
     * 定义任务的默认跳转目标（无条件）
     * @param nextStepName 跳转目标节点名称
     */
    abstract def nextTo(String nextStepName);

    /**
     * DSL关键词：onError
     * 定义任务级别的错误处理逻辑
     * @param c 错误处理闭包，委托为FlowTaskErrorHandlingAccess
     * @return NextHint 用于链式指定错误跳转目标
     */
    abstract NextHint onError(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> c)
}