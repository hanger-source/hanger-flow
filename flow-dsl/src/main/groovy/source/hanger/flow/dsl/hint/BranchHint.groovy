package source.hanger.flow.dsl.hint

import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess
import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * BranchHint 是 parallel/async 分支定义的语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 branch 'xxx' when { ... } 语法块内允许出现的所有关键词和结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束分支定义的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * branch '发送邮件' when { context.needEmail }
 * </pre>
 * <p>
 * 设计说明：
 *   - 只允许出现when方法，明确分支的条件表达式
 *   - 便于后续扩展分支属性（如优先级、标签等）
 */
trait BranchHint {
    /**
     * DSL关键词：when
     * 定义分支的条件表达式
     * @param conditionClosure 条件闭包，委托为FlowRuntimePredicateAccess
     */
    abstract void when(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure)
}