package source.hanger.flow.dsl

import groovy.transform.CompileStatic
import groovy.transform.Internal
import source.hanger.flow.contract.model.AsyncStepDefinition
import source.hanger.flow.contract.model.FlowDefinition
import source.hanger.flow.contract.model.ParallelStepDefinition
import source.hanger.flow.contract.model.TaskStepDefinition
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess
import source.hanger.flow.dsl.hint.FlowHint
import source.hanger.flow.util.ClosureUtils

import java.util.concurrent.atomic.AtomicBoolean

import static groovy.lang.Closure.DELEGATE_FIRST
import static source.hanger.flow.util.ClosureUtils.*
import static source.hanger.flow.util.DslValidationUtils.ensureSingleDefinition
import static source.hanger.flow.util.DslValidationUtils.getUnknownPropertyReadErrorMessage

/**
 * 流程DSL主构建器
 * 负责解析flow { ... } DSL块，将Groovy闭包映射为流程模型（FlowDefinition）
 * 支持流程元信息、全局onEnter/onError、任务、并行、异步等DSL语法
 */
@CompileStatic
class FlowBuilder implements FlowHint {
    @Internal
    /** 当前正在构建的流程定义对象 */
    private FlowDefinition flowDefinition // 当前正在构建的流程定义
    @Internal
    /** 标记是否已定义onEnter，保证DSL唯一性 */
    private AtomicBoolean hasOnEnterDefined = new AtomicBoolean(false)
    @Internal
    /** 标记是否已定义onError，保证DSL唯一性 */
    private AtomicBoolean hasOnErrorDefined = new AtomicBoolean(false)

    /** 支持读取的流程元信息属性 */
    private static final List<String> SUPPORTED_PROPERTIES_FOR_READ = ['version', 'name', 'description']

    /**
     * 构造方法，初始化流程定义
     */
    FlowBuilder() {
        flowDefinition = new FlowDefinition()
    }

    /**
     * DSL关键词：version
     * 设置流程版本号
     */
    void version(String text) {
        flowDefinition.version = text
    }

    /**
     * DSL关键词：name
     * 设置流程名称
     */
    void name(String text) {
        flowDefinition.name = text
    }

    /**
     * DSL关键词：description
     * 设置流程描述信息
     */
    void description(String text) {
        flowDefinition.description = text.stripIndent().strip()
    }

    /**
     * DSL关键词：start
     * 定义流程起始任务（自动命名为START）
     * @param text 起始任务的下一个节点名称
     */
    void start(String text) {
        // 相当于传入一个默认为START的task
        internalTask {
            name FlowDslEntry.START
            nextTo text
        }
    }

    /**
     * DSL关键词：onEnter
     * 定义流程进入时的全局处理逻辑
     * @param enterClosure Groovy闭包，最终封装为Java接口
     */
    void onEnter(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> enterClosure) {
        // 适配闭包的执行逻辑为java的实现
        ensureSingleDefinition(hasOnEnterDefined, "flow.onEnter", {
            flowDefinition.enterHandingRunnable = toFlowClosure(enterClosure)
        })
    }

    /**
     * DSL关键词：onError
     * 定义流程全局错误处理逻辑
     * @param errorClosure Groovy闭包，最终封装为Java接口
     * @return NextBuilder 用于链式指定错误跳转目标
     */
    NextBuilder onError(@DelegatesTo(value = FlowRuntimeExecuteAccess, strategy = DELEGATE_FIRST) Closure<?> errorClosure) {
        ensureSingleDefinition(hasOnErrorDefined, "flow.onError", {
            flowDefinition.errorHandingRunnable = toFlowClosure(errorClosure)
        })
        // 虚拟flow节点
        internalTask { name FlowDslEntry.FLOW_GLOBAL_STEP }.next TRUE
    }

    /**
     * DSL关键词：task
     * 定义流程中的任务节点
     * @param closure 任务构建闭包
     */
    void task(@DelegatesTo(value = TaskBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        internalTask(closure)
    }

    @Internal
    /**
     * 内部方法，实际构建任务节点
     * @param closure 任务构建闭包
     * @return TaskBuilder
     */
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

    /**
     * DSL关键词：parallel
     * 定义并行处理块
     * @param closure 并行块构建闭包
     */
    void parallel(@DelegatesTo(value = ParallelBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def stepDefinition = new ParallelStepDefinition()
        flowDefinition.addStep(stepDefinition)
        def parallelBuilder = new ParallelBuilder(stepDefinition)
        closure.delegate = parallelBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
    }

    /**
     * DSL关键词：async
     * 定义异步分支块
     * @param closure 异步块构建闭包
     */
    void async(@DelegatesTo(value = AsyncBuilder, strategy = DELEGATE_FIRST) Closure<?> closure) {
        def stepDefinition = new AsyncStepDefinition()
        flowDefinition.addStep(stepDefinition)
        def asyncBuilder = new AsyncBuilder(stepDefinition)
        closure.delegate = asyncBuilder
        closure.resolveStrategy = DELEGATE_FIRST
        closure.call()
    }

    /**
     * 处理DSL中未定义的方法调用（如错误拼写等）
     * @param name 方法名
     * @param args 参数
     */
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

    /**
     * 处理DSL中未定义的属性读取
     * @param name 属性名
     */
    def propertyMissing(String name) {
        def errorMessage = getUnknownPropertyReadErrorMessage('flow', name, SUPPORTED_PROPERTIES_FOR_READ)
        throw new MissingPropertyException(errorMessage, name, this.class)
    }

    /**
     * 获取最终构建的流程定义模型
     * @return FlowDefinition
     */
    FlowDefinition getFlowDefinition() {
        return this.flowDefinition
    }
}