package source.hanger.flow.dsl

import groovy.transform.Internal
import source.hanger.flow.contract.constant.FlowConstants
import source.hanger.flow.contract.model.FlowDefinition
import source.hanger.flow.dsl.hint.FlowHint

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * 流程DSL入口类
 * 提供flow { ... } 入口方法，定义流程的起点
 * 包含流程特殊常量（END、START、ERROR等）
 */
class FlowDslEntry {

    @Internal
    /** 流程结束标记常量 */
    public static String END = FlowConstants.END
    @Internal
    /** 流程起始标记常量 */
    public static String START = FlowConstants.START
    @Internal
    /** 流程全局虚拟节点常量 */
    public static String FLOW_GLOBAL_STEP = FlowConstants.FLOW_GLOBAL_STEP

    /**
     * DSL入口方法：flow { ... }
     * 解析Groovy DSL闭包，构建流程定义模型
     * @param closure 流程DSL闭包
     * @return FlowDefinition 流程定义模型
     */
    static FlowDefinition flow(@DelegatesTo(value = FlowHint, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def builder = new FlowBuilder()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        // 执行 DSL 闭包，开始构建流程
        closure.call()
        builder.getFlowDefinition()
    }
}