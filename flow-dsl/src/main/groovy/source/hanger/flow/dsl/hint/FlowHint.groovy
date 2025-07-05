package source.hanger.flow.dsl.hint

import groovy.transform.CompileStatic
import source.hanger.flow.contract.runtime.flow.access.FlowEnterHandlingAccess
import source.hanger.flow.contract.runtime.flow.access.FlowErrorHandlingAccess

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * FlowHint 是流程DSL的顶层语法提示接口（trait）
 * <p>
 * 作用：
 *   - 规定了 flow { ... } DSL 块内允许出现的所有顶级关键词和语法结构
 *   - 为IDE智能提示、类型检查、DSL扩展提供基础
 *   - 约束DSL的结构化写法，防止拼写错误和语法歧义
 * <p>
 * 典型用法：
 * <pre>
 * flow {
 *   version '1.0.0'
 *   name '订单流程'
 *   description '处理订单的主流程'
 *   start '初始化'
 *   onEnter { ... }
 *   onError { ... } to '错误处理'
 *   task { ... }
 *   async { ... }
 *   parallel { ... }
 * }
 * </pre>
 * <p>
 * 设计说明：
 *   - 每个abstract方法对应一个DSL关键词，参数类型和注解决定了闭包的委托类型和语法结构
 *   - 便于后续扩展新的DSL语法（如subflow、event等）
 *   - 强制所有DSL实现都必须支持这些顶级语法
 */
@CompileStatic
trait FlowHint {
    /**
     * DSL关键词：version
     * 设置流程版本号
     * @param text 版本号字符串
     */
    abstract void version(String text)

    /**
     * DSL关键词：name
     * 设置流程名称
     * @param text 流程名称
     */
    abstract void name(String text)

    /**
     * DSL关键词：description
     * 设置流程描述信息
     * @param text 流程描述
     */
    abstract void description(String text)

    /**
     * DSL关键词：start
     * 设置流程的起始任务节点
     * @param text 起始任务名称
     */
    abstract void start(String text)

    /**
     * DSL关键词：onEnter
     * 定义流程进入时的全局处理逻辑
     * @param c 处理闭包，委托为FlowEnterHandlingAccess
     */
    abstract void onEnter(@DelegatesTo(value = FlowEnterHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> c)

    /**
     * DSL关键词：onError
     * 定义流程全局错误处理逻辑
     * @param c 错误处理闭包，委托为FlowErrorHandlingAccess
     * @return NextHint 用于链式指定错误跳转目标
     */
    abstract NextHint onError(@DelegatesTo(value = FlowErrorHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> c)

    /**
     * DSL关键词：task
     * 定义流程中的任务节点
     * @param c 任务构建闭包，委托为TaskHint
     */
    abstract void task(@DelegatesTo(value = TaskHint, strategy = DELEGATE_FIRST) Closure<?> c)

    /**
     * DSL关键词：async
     * 定义异步分支块
     * @param c 异步块构建闭包，委托为AsyncHint
     */
    abstract void async(@DelegatesTo(value = AsyncHint, strategy = DELEGATE_FIRST) Closure<?> c)

    /**
     * DSL关键词：parallel
     * 定义并行处理块
     * @param c 并行块构建闭包，委托为ParallelHint
     */
    abstract void parallel(@DelegatesTo(value = ParallelHint, strategy = DELEGATE_FIRST) Closure<?> c);
}