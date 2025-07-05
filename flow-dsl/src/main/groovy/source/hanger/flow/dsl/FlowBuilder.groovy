package source.hanger.flow.dsl

import groovy.transform.CompileStatic
import groovy.transform.Internal
import source.hanger.flow.contract.model.AsyncStepDefinition
import source.hanger.flow.contract.model.FlowDefinition
import source.hanger.flow.contract.model.ParallelStepDefinition
import source.hanger.flow.contract.model.TaskStepDefinition
import source.hanger.flow.contract.runtime.flow.access.FlowEnterHandlingAccess
import source.hanger.flow.contract.runtime.flow.access.FlowErrorHandlingAccess
import source.hanger.flow.contract.runtime.flow.function.FlowEnterHandingRunnable
import source.hanger.flow.contract.runtime.flow.function.FlowErrorHandingRunnable
import source.hanger.flow.dsl.hint.FlowHint
import source.hanger.flow.util.ClosureUtils

import java.util.concurrent.atomic.AtomicBoolean

import static groovy.lang.Closure.DELEGATE_FIRST
import static source.hanger.flow.util.DslValidationUtils.ensureSingleDefinition
import static source.hanger.flow.util.DslValidationUtils.getUnknownPropertyReadErrorMessage

@CompileStatic
class FlowBuilder implements FlowHint {
    @Internal
    // 标记为内部属性，不应该在 DSL 中直接访问
    private FlowDefinition flowDefinition // 当前正在构建的流程定义
    @Internal
    private AtomicBoolean hasOnEnterDefined = new AtomicBoolean(false)
    @Internal
    private AtomicBoolean hasOnErrorDefined = new AtomicBoolean(false)

    private static final List<String> SUPPORTED_PROPERTIES_FOR_READ = ['version', 'name', 'description']


    FlowBuilder() {
        flowDefinition = new FlowDefinition()
    }

    void version(String text) {
        flowDefinition.version = text
    }

    void name(String text) {
        flowDefinition.name = text
    }

    void description(String text) {
        flowDefinition.description = text.stripIndent().strip()
    }

    void start(String text) {
        // 相当于传入一个默认为START的task
        internalTask {
            name FlowDslEntry.START
            nextTo text
        }
    }

    void onEnter(@DelegatesTo(value = FlowEnterHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> enterClosure) {
        // 适配闭包的执行逻辑为java的实现
        ensureSingleDefinition(hasOnEnterDefined, "flow.onEnter", {
            flowDefinition.enterHandingRunnable = new FlowEnterHandingRunnable() {
                @Override
                void handle(FlowEnterHandlingAccess access) {
                    enterClosure.delegate = access
                    enterClosure.resolveStrategy = DELEGATE_FIRST
                    enterClosure.call()
                }
            }
        })
    }

    NextBuilder onError(@DelegatesTo(value = FlowErrorHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> errorClosure) {
        ensureSingleDefinition(hasOnErrorDefined, "flow.onError", {
            flowDefinition.errorHandingRunnable = new FlowErrorHandingRunnable() {

                @Override
                void handle(FlowErrorHandlingAccess access) {
                    errorClosure.delegate = access
                    errorClosure.resolveStrategy = DELEGATE_FIRST
                    errorClosure.call()
                }
            }
        })
        // 虚拟flow节点
        internalTask { name FlowDslEntry.FLOW_GLOBAL_STEP }.next ClosureUtils.TRUE
    }

    void task(@DelegatesTo(value = TaskBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        internalTask(closure)
    }

    @Internal
    private TaskBuilder internalTask(@DelegatesTo(value = TaskBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def stepDefinition = new TaskStepDefinition()
        // 如果是流程的第一个任务，则设置为起始节点
        flowDefinition.addStep(stepDefinition)
        def taskBuilder = new TaskBuilder(stepDefinition)
        // 传入 FlowBuilder 引用和当前任务节点
        closure.delegate = taskBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
        return taskBuilder
    }

    // DSL 关键词: parallel
    // ParallelBuilder 作为闭包的 delegate
    void parallel(@DelegatesTo(value = ParallelBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def stepDefinition = new ParallelStepDefinition()
        flowDefinition.addStep(stepDefinition)
        def parallelBuilder = new ParallelBuilder(stepDefinition)
        closure.delegate = parallelBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
    }

    // DSL 关键词: async
    // AsyncBuilder 作为闭包的 delegate
    void async(@DelegatesTo(value = AsyncBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def stepDefinition = new AsyncStepDefinition()
        flowDefinition.addStep(stepDefinition)
        def asyncBuilder = new AsyncBuilder(stepDefinition)
        closure.delegate = asyncBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
    }

    // 用于在 task/entry/parallel 内部，链接到 FlowBuilder 的下一个方法
    def methodMissing(String name, args) {
        if (["task", "async", "parallel"].contains(name)) {
            // 如果方法是 DSL 关键词，且当前没有对应的处理，
            // 尝试将它转发给 FlowBuilder
            // 这里为了简化，直接判断关键词，实际需要更严谨的委托链
//            if (flowDefinition.metaClass.hasMethod(this, name, args as Object[])) {
//                return flowDefinition.metaClass.invokeMethod(this, name, args)
//            }
        }
        throw new MissingMethodException(name, flowDefinition.getClass(), args)
    }

    // 处理读取不存在的属性
    def propertyMissing(String name) {
        def errorMessage = getUnknownPropertyReadErrorMessage('flow', name, SUPPORTED_PROPERTIES_FOR_READ)
        throw new MissingPropertyException(errorMessage, name, this.class)
    }

    FlowDefinition getFlowDefinition() {
        return this.flowDefinition
    }
}