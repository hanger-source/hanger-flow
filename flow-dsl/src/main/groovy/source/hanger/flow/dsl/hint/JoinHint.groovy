package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic

/**
 * JoinHint 是并行块waitFor汇合点的语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 waitFor ... nextTo ... 语法块内允许出现的所有关键词和结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束汇合点定义的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * waitFor '分支A', '分支B' nextTo '下一步'
 * </pre>
 * <p>
 * 设计说明：
 *   - 只允许出现nextTo方法，明确汇合后的跳转目标
 *   - 便于后续扩展汇合点属性（如超时、策略等）
 */
@CompileStatic
trait JoinHint {
    /**
     * DSL关键词：nextTo
     * 指定汇合点的跳转目标
     * @param nextStepName 跳转目标节点名称
     */
    abstract void nextTo(String nextStepName)
}