package source.hanger.flow.dsl.hint

/**
 * AsyncHint 是 async { ... } DSL块的语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 async { ... } 块内允许出现的所有关键词和语法结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束异步分支块的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * async {
 *   name '异步通知流'
 *   description '用于异步发送通知'
 *   branch '发送短信通知'
 * }
 * </pre>
 * <p>
 * 设计说明：
 *   - 每个abstract方法对应一个DSL关键词，参数类型和注解决定了闭包的委托类型和语法结构
 *   - 便于后续扩展异步块DSL语法
 *   - 强制所有async DSL实现都必须支持这些语法
 */
trait AsyncHint {
    /**
     * DSL关键词：name
     * 设置异步分支块名称
     * @param text 异步块名称
     */
    abstract void name(String text)

    /**
     * DSL关键词：description
     * 设置异步分支块描述信息
     * @param text 异步块描述
     */
    abstract void description(String text)

    /**
     * DSL关键词：branch
     * 定义异步分支目标
     * @param text 分支目标任务名称
     */
    abstract void branch(String text)
}