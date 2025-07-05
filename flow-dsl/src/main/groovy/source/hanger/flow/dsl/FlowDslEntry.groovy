package source.hanger.flow.dsl

import groovy.transform.Internal
import source.hanger.flow.contract.model.FlowDefinition
import source.hanger.flow.dsl.hint.FlowHint

import static groovy.lang.Closure.DELEGATE_FIRST

class FlowDslEntry {

    @Internal
    public static String END = "__END__"
    @Internal
    public static String START = "__START__"
    @Internal
    public static String ERROR = "__ERROR__"
    @Internal
    public static String FLOW_GLOBAL_STEP = "__FLOW_GLOBAL_STEP__"

    static FlowDefinition flow(@DelegatesTo(value = FlowHint, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def builder = new FlowBuilder()
        closure.delegate = builder
        closure.resolveStrategy = DELEGATE_FIRST
        // 执行 DSL 闭包，开始构建流程
        closure.call()
        builder.getFlowDefinition()
    }
}