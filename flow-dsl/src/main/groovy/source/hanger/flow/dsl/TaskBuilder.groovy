package source.hanger.flow.dsl

import groovy.transform.Internal
import source.hanger.flow.contract.model.TaskStepDefinition
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicateAccess
import source.hanger.flow.contract.runtime.task.access.FlowTaskEnterHandingAccess
import source.hanger.flow.contract.runtime.task.access.FlowTaskErrorHandlingAccess
import source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess
import source.hanger.flow.contract.runtime.task.function.FlowTaskEnterHandingRunnable
import source.hanger.flow.contract.runtime.task.function.FlowTaskErrorHandingRunnable
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable
import source.hanger.flow.dsl.hint.TaskHint
import source.hanger.flow.util.ClosureUtils

import java.util.concurrent.atomic.AtomicBoolean

import static groovy.lang.Closure.DELEGATE_FIRST
import static source.hanger.flow.util.DslValidationUtils.ensureSingleDefinition

/**
 * 任务节点DSL构建器
 * 负责解析task { ... } DSL块，将Groovy闭包映射为任务模型（TaskStepDefinition）
 * 支持任务元信息、onEnter、run、next、onError等DSL语法
 */
class TaskBuilder implements TaskHint {
    @Internal
    /** 当前正在构建的任务节点模型 */
    private TaskStepDefinition taskStepDefinition
    @Internal
    /** 标记是否已定义onEnter，保证DSL唯一性 */
    private AtomicBoolean hasOnEnterDefined = new AtomicBoolean(false)
    @Internal
    /** 标记是否已定义onError，保证DSL唯一性 */
    private AtomicBoolean hasOnErrorDefined = new AtomicBoolean(false)
    @Internal
    /** 标记是否已定义run，保证DSL唯一性 */
    private AtomicBoolean hasRunDefined = new AtomicBoolean(false)

    /**
     * 构造方法，初始化任务节点模型
     * @param taskStepDefinition 任务节点模型
     */
    TaskBuilder(TaskStepDefinition taskStepDefinition) {
        this.taskStepDefinition = taskStepDefinition
    }

    /**
     * DSL关键词：name
     * 设置任务名称
     */
    void name(String text) {
        taskStepDefinition.name = text
    }

    /**
     * DSL关键词：description
     * 设置任务描述信息
     */
    void description(String text) {
        taskStepDefinition.description = text
    }

    /**
     * DSL关键词：onEnter
     * 定义任务进入时的处理逻辑
     * @param enterClosure Groovy闭包，最终封装为Java接口
     */
    void onEnter(@DelegatesTo(value = FlowTaskEnterHandingAccess, strategy = DELEGATE_FIRST) Closure<?> enterClosure) {
        // 适配闭包的执行逻辑为java的实现
        ensureSingleDefinition(hasOnEnterDefined, "task.onEnter", {
            taskStepDefinition.enterHandingRunnable = new FlowTaskEnterHandingRunnable() {
                @Override
                void handle(FlowTaskEnterHandingAccess access) {
                    enterClosure.delegate = access
                    enterClosure.resolveStrategy = DELEGATE_FIRST
                    enterClosure.call()
                }
            }
        })
    }

    /**
     * DSL关键词：run
     * 定义任务的核心执行逻辑
     * @param runClosure Groovy闭包，最终封装为Java接口
     */
    void run(@DelegatesTo(value = FlowTaskRunAccess, strategy = DELEGATE_FIRST) Closure<?> runClosure) {
        // 适配闭包的执行逻辑为java的实现
        ensureSingleDefinition(hasRunDefined, "task.run", {
            taskStepDefinition.taskRunnable = new FlowTaskRunnable() {
                @Override
                void run(FlowTaskRunAccess access) {
                    runClosure.delegate = access
                    runClosure.resolveStrategy = DELEGATE_FIRST
                    runClosure.call()
                }
            }
        })
    }

    /**
     * DSL关键词：next
     * 定义任务的条件跳转分支
     * @param conditionClosure 条件闭包
     * @return NextBuilder 用于链式指定跳转目标
     */
    NextBuilder next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure) {
        return new NextBuilder(taskStepDefinition, conditionClosure) // next 的目标是 FlowBuilder 管理的节点
    }

    /**
     * DSL关键词：nextTo
     * 定义任务的默认跳转目标（无条件）
     * @param nextStepName 跳转目标节点名称
     */
    def nextTo(String nextStepName) {
        next ClosureUtils.TRUE to nextStepName
    }

    /**
     * DSL关键词：onError
     * 定义任务级别的错误处理逻辑
     * @param errorClosure Groovy闭包，最终封装为Java接口
     * @return NextBuilder 用于链式指定错误跳转目标
     */
    NextBuilder onError(@DelegatesTo(value = FlowTaskErrorHandlingAccess, strategy = DELEGATE_FIRST) Closure<?> errorClosure) {
        ensureSingleDefinition(hasOnErrorDefined, "Flow.onError", {
            taskStepDefinition.errorHandingRunnable = new FlowTaskErrorHandingRunnable() {
                @Override
                void handle(FlowTaskErrorHandlingAccess access) {
                    errorClosure.delegate = access
                    errorClosure.resolveStrategy = DELEGATE_FIRST
                    errorClosure.call()
                }
            }
        })
        next ClosureUtils.TRUE
    }
}