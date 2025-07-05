package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic

/**
 * NextHint 是条件跳转/错误跳转的语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 next { ... } to '目标'、onError { ... } to '目标' 语法块内允许出现的所有关键词和结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束跳转定义的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * next { context.flag } to '下一步'
 * onError { ... } to '错误处理'
 * </pre>
 * <p>
 * 设计说明：
 *   - 只允许出现to方法，明确跳转目标
 *   - 便于后续扩展跳转属性（如条件优先级、标签等）
 */
@CompileStatic
trait NextHint {
    /**
     * DSL关键词：to
     * 指定条件跳转/错误跳转的目标节点
     * @param nextStepName 跳转目标节点名称
     */
    abstract void to(String nextStepName)
}