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

// TaskBuilder 处理 task 关键词内部的逻辑
class TaskBuilder implements TaskHint {
    @Internal
    private TaskStepDefinition taskStepDefinition
    @Internal
    private AtomicBoolean hasOnEnterDefined = new AtomicBoolean(false)
    @Internal
    private AtomicBoolean hasOnErrorDefined = new AtomicBoolean(false)

    TaskBuilder(TaskStepDefinition taskStepDefinition) {
        this.taskStepDefinition = taskStepDefinition
    }

    void name(String text) {
        taskStepDefinition.name = text
    }

    // DSL 关键词: description (在 task 内部)
    void description(String text) {
        taskStepDefinition.description = text
    }

    void onEnter(@DelegatesTo(value = FlowTaskEnterHandingAccess, strategy = DELEGATE_FIRST) Closure<?> enterClosure) {
        // 适配闭包的执行逻辑为java的实现
        ensureSingleDefinition(hasOnEnterDefined, "Task.onEnter", {
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

    // DSL 关键词: run
    void run(@DelegatesTo(value = FlowTaskRunAccess, strategy = DELEGATE_FIRST) Closure<?> runClosure) {
        // 适配闭包的执行逻辑为java的实现
        taskStepDefinition.taskRunnable = new FlowTaskRunnable() {
            @Override
            void run(FlowTaskRunAccess access) {
                runClosure.delegate = access
                runClosure.resolveStrategy = DELEGATE_FIRST
                runClosure.call()
            }
        }
    }

    // DSL 关键词: next (在 task 内部)
    NextBuilder next(@DelegatesTo(value = FlowRuntimePredicateAccess, strategy = DELEGATE_FIRST) Closure<?> conditionClosure) {
        return new NextBuilder(taskStepDefinition, conditionClosure) // next 的目标是 FlowBuilder 管理的节点
    }

    def nextTo(String nextStepName) {
        next ClosureUtils.TRUE to nextStepName
    }

    // DSL 关键词: onError (在 task 内部)
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