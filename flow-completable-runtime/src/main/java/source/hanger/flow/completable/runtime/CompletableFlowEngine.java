package source.hanger.flow.completable.runtime;

import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimeExecuteAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimeExecuteContext;
import source.hanger.flow.completable.runtime.error.DefaultFlowErrorHandler;
import source.hanger.flow.completable.runtime.error.DefaultStepErrorHandler;
import source.hanger.flow.completable.runtime.step.AsyncStepExecutor;
import source.hanger.flow.completable.runtime.step.ParallelStepExecutor;
import source.hanger.flow.completable.runtime.step.TaskStepExecutor;
import source.hanger.flow.contract.model.*;
import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;
import source.hanger.flow.core.runtime.engine.FlowEngine;
import source.hanger.flow.core.runtime.engine.FlowExecutionHandle;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.step.StepExecutor;
import source.hanger.flow.core.runtime.lifecycle.FlowLifecycleHandler;
import source.hanger.flow.core.runtime.lifecycle.StepLifecycleHandler;
import source.hanger.flow.completable.runtime.lifecycle.DefaultFlowLifecycleHandler;
import source.hanger.flow.completable.runtime.lifecycle.DefaultStepLifecycleHandler;
import source.hanger.flow.core.util.FlowLogContext;
import source.hanger.flow.core.util.FlowLogger;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.status.FlowStepStatus;
import source.hanger.flow.core.runtime.error.StepErrorHandler;
import source.hanger.flow.core.runtime.error.FlowErrorHandler;
import source.hanger.flow.core.runtime.channel.DefaultFlowChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static java.util.concurrent.CompletableFuture.*;
import static source.hanger.flow.contract.constant.FlowConstants.*;
import static source.hanger.flow.core.util.FlowStructureUtils.*;

/**
 * CompletableFuture流程引擎
 * <p>
 * 作用：
 * - 基于CompletableFuture实现异步流程执行
 * - 支持任务、并行、异步分支等所有流程节点类型
 * - 提供完整的错误处理和生命周期管理
 * - 通过channel实现step间的异步数据交换
 * <p>
 * 设计特点：
 * - 完全异步执行，支持高并发
 * - 基于CompletableFuture的链式调用
 * - 通过channel实现step间解耦
 * - 支持流程状态跟踪和监控
 * - 提供丰富的执行上下文和访问接口
 * <p>
 * 典型用法：
 * FlowDefinition flowDef = ...; // 从DSL解析得到
 * CompletableFlowEngine engine = new CompletableFlowEngine();
 * CompletableFuture<FlowResult> future = engine.execute(flowDef);
 * FlowResult result = future.get(); // 等待完成
 */
public class CompletableFlowEngine implements FlowEngine<Object> {

    private static final Logger log = LoggerFactory.getLogger(CompletableFlowEngine.class);

    /**
     * 默认线程池
     */
    private final Executor executor;

    /**
     * 流程执行状态跟踪
     */
    private final Map<String, FlowExecutionState> executionStates = new ConcurrentHashMap<>();

    /**
     * 步骤类型到执行器的映射
     */
    private final Map<Class<?>, StepExecutor<?>> stepExecutors = new ConcurrentHashMap<>();

    /**
     * 步骤错误处理策略
     */
    private final StepErrorHandler stepErrorHandler;

    /**
     * 流程错误处理策略
     */
    private final FlowErrorHandler flowErrorHandler;

    /**
     * 流程生命周期处理器
     */
    private final FlowLifecycleHandler flowLifecycleHandler;

    /**
     * 步骤生命周期处理器
     */
    private final StepLifecycleHandler stepLifecycleHandler;

    /**
     * 使用默认线程池创建引擎
     */
    public CompletableFlowEngine() {
        this(Runnable::run);
    }

    /**
     * 使用指定线程池创建引擎
     *
     * @param executor 线程池
     */
    public CompletableFlowEngine(Executor executor) {
        this(executor, new DefaultStepErrorHandler(), new DefaultFlowErrorHandler());
    }

    /**
     * 使用自定义错误处理策略创建引擎
     */
    public CompletableFlowEngine(Executor executor, StepErrorHandler stepErrorHandler,
        FlowErrorHandler flowErrorHandler) {
        this.executor = executor;
        this.stepErrorHandler = stepErrorHandler;
        this.flowErrorHandler = flowErrorHandler;
        flowLifecycleHandler = new DefaultFlowLifecycleHandler();
        stepLifecycleHandler = new DefaultStepLifecycleHandler();
        // 注册执行器
        stepExecutors.put(TaskStepDefinition.class, new TaskStepExecutor(executor));
        stepExecutors.put(ParallelStepDefinition.class, new ParallelStepExecutor(executor));
        stepExecutors.put(AsyncStepDefinition.class, new AsyncStepExecutor(executor));
    }

    /**
     * 使用自定义生命周期处理器创建引擎
     */
    public CompletableFlowEngine(Executor executor, StepErrorHandler stepErrorHandler,
        FlowErrorHandler flowErrorHandler,
        FlowLifecycleHandler flowLifecycleHandler, StepLifecycleHandler stepLifecycleHandler) {
        this.executor = executor;
        this.stepErrorHandler = stepErrorHandler;
        this.flowErrorHandler = flowErrorHandler;
        this.flowLifecycleHandler = flowLifecycleHandler;
        this.stepLifecycleHandler = stepLifecycleHandler;
        // 注册执行器
        stepExecutors.put(TaskStepDefinition.class, new TaskStepExecutor(executor));
        stepExecutors.put(ParallelStepDefinition.class, new ParallelStepExecutor(executor));
        stepExecutors.put(AsyncStepDefinition.class, new AsyncStepExecutor(executor));
    }

    @Override
    public FlowExecutionHandle start(FlowDefinition flowDefinition, Map<String, Object> inputs) {
        String executionId = generateExecutionId(flowDefinition);
        FlowExecutionContext context = new FlowExecutionContext(executionId, flowDefinition, inputs);
        // 创建channel并设置到context
        FlowStreamingChannel channel = new DefaultFlowChannel("flow-" + executionId);
        context.setChannel(channel);

        FlowLogContext flowLogContext = new FlowLogContext(context, FLOW_GLOBAL_STEP);
        FlowLogger.info(flowLogContext, "🚀 流程开始执行");

        CompletableFuture<FlowResult> future = supplyAsync(() -> {
            try {
                // 执行流程进入回调
                executeFlowEnter(flowDefinition, context);
                // 执行所有步骤
                FlowResult result = executeSteps(flowDefinition, context);
                FlowLogger.info(flowLogContext, "🎉 流程执行完成");
                return result;
            } catch (Exception e) {
                FlowLogger.error(flowLogContext, "❌ 流程执行异常: " + e.getMessage(), e);
                flowErrorHandler.handleError(flowDefinition, context, e);
                return FlowResult.error(context, e, FLOW_GLOBAL_STEP);
            }
        }, executor);

        return new CompletableFlowExecutionHandle(executionId, future);
    }

    @Override
    public CompletableFuture<FlowResult> execute(FlowDefinition flowDefinition, Map<String, Object> initialParams) {
        return start(flowDefinition, initialParams).future();
    }

    @Override
    public boolean stop(String executionId) {
        // 实现停止逻辑
        FlowExecutionState state = executionStates.get(executionId);
        if (state == FlowExecutionState.RUNNING) {
            executionStates.put(executionId, FlowExecutionState.STOPPED);
            return true;
        }
        return false;
    }

    @Override
    public FlowExecutionState getExecutionState(String executionId) {
        return executionStates.getOrDefault(executionId, FlowExecutionState.PENDING);
    }

    private void executeFlowEnter(FlowDefinition flowDefinition, FlowExecutionContext context) {
        flowLifecycleHandler.onFlowStart(flowDefinition, context);
    }

    private FlowResult executeSteps(FlowDefinition flowDefinition, FlowExecutionContext context) {
        StepDefinition startStep = findStartStep(flowDefinition);
        if (startStep == null) {
            throw new IllegalStateException("找不到开始步骤");
        }
        FlowLogContext flowLogContext = new FlowLogContext(context, startStep);
        // 使用新的链式执行方式
        return executeStepChain(startStep, context, flowLogContext).thenApply(result -> {
            FlowLogger.info(flowLogContext.stepName(result.getStepName()), "步骤执行完成");
            return result;
        }).exceptionally(error -> {
            FlowLogger.error(flowLogContext, "步骤执行失败", error.getMessage(), error);
            return handleFlowError(flowDefinition, context, (Exception)error);
        }).join(); // 等待完成
    }

    /**
     * 链式执行步骤
     * 基于CompletableFuture的thenAccept等能力解耦每个step
     */
    private CompletableFuture<FlowResult> executeStepChain(StepDefinition step, FlowExecutionContext context,
        FlowLogContext flowLogContext) {
        if (step == null) {
            throw new IllegalStateException("找不到步骤");
        }
        FlowLogger.info(flowLogContext, step.getName());
        return executeStepAsync(step, context).thenCompose(stepResult -> {
                String completedStepName = stepResult.getStepName();
                FlowLogContext logContext = flowLogContext.stepName(completedStepName);
                // 根据步骤结果决定下一步
                List<Transition> transitions = collectTransitions(step);
                StepDefinition nextStep = evaluateTransitions(step, transitions, context, flowLogContext);
                if (nextStep != null) {
                    FlowLogger.info(logContext, "步骤 {} 跳转到步骤 {}", completedStepName, nextStep.getName());
                    return executeStepChain(nextStep, context, flowLogContext);
                } else {
                    FlowLogger.info(logContext, "步骤 {} 执行完成，流程结束", completedStepName);
                    return completedFuture(FlowResult.success(context, completedStepName));
                }
            })
            .exceptionally(error -> {
                FlowLogger.error(flowLogContext, step.getName(), error);
                return handleStepError(flowLogContext, (Exception)error);
            });
    }

    /**
     * 异步执行单个步骤
     */
    private CompletableFuture<FlowResult> executeStepAsync(StepDefinition step, FlowExecutionContext context) {
        return supplyAsync(() -> {
            try {
                updateExecutionState(step.getName(), FlowStepStatus.RUNNING);

                // 创建步骤上下文
                CompletableFlowRuntimeExecuteContext stepContext =
                    new CompletableFlowRuntimeExecuteContext(context, step);

                // 执行步骤
                CompletableFuture<FlowResult> resultFuture = executeStepByType(step, stepContext);
                updateExecutionState(step.getName(), FlowStepStatus.COMPLETED);
                return resultFuture;

            } catch (Exception e) {
                updateExecutionState(step.getName(), FlowStepStatus.ERROR);
                throw e;
            }
        }, executor).thenCompose(future -> future);
    }

    private StepDefinition findStartStep(FlowDefinition flowDefinition) {
        return flowDefinition.getStepDefinitions().stream()
            .filter(step -> "__START__".equals(step.getName()))
            .findFirst()
            .orElse(null);
    }

    private FlowResult handleFlowError(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error) {
        flowErrorHandler.handleError(flowDefinition, context, error);
        return FlowResult.error(error);
    }

    private String generateExecutionId(FlowDefinition flowDefinition) {
        return flowDefinition.getName() + "-" + System.currentTimeMillis();
    }

    private StepDefinition evaluateTransitions(StepDefinition currentStep, List<Transition> transitions,
        FlowExecutionContext context, FlowLogContext flowLogContext) {
        if (transitions.isEmpty()) {
            return null; // 没有后续步骤
        }
        // 完整实现：评估所有转换条件
        for (Transition transition : transitions) {
            try {
                // 创建访问接口用于条件评估
                CompletableFlowRuntimeExecuteContext stepContext
                    = new CompletableFlowRuntimeExecuteContext(context, currentStep);
                CompletableFlowRuntimeExecuteAccess access
                    = new CompletableFlowRuntimeExecuteAccess(stepContext);
                // 评估条件
                if (transition.flowRuntimePredicate().test(access)) {
                    return findStepDefinition(context.getFlowDefinition(), transition.nextStepName());
                }
            } catch (Exception e) {
                FlowLogger.error(flowLogContext, "评估流转条件失败: {}", transition, e);
            }
        }
        return null;
    }

    private CompletableFuture<FlowResult> executeStepByType(StepDefinition step,
        CompletableFlowRuntimeExecuteContext stepContext) {
        StepType stepType = step.getStepType();
        log.info("执行步骤类型: {}", stepType);
        StepExecutor<?> executor = stepExecutors.get(step.getClass());
        if (executor == null) {
            throw new UnsupportedOperationException("不支持的步骤类型: " + stepType);
        }
        FlowLogContext flowLogContext = new FlowLogContext(stepContext.getFlowContext(), step);
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 执行步骤 - 异步执行，不等待完成
                executor.execute(step, stepContext.getFlowContext());
                // 直接返回成功，让 channel 处理数据流转
                return FlowResult.success(stepContext.getFlowContext());
            } catch (Exception e) {
                log.error("步骤执行失败: {}", step.getName(), e);
                return handleStepError(flowLogContext, e);
            }
        }, this.executor);
    }

    private FlowResult handleStepError(FlowLogContext flowLogContext, Exception error) {
        FlowLogger.error(flowLogContext, "步骤执行错误: {}");
        return FlowResult.error(error);
    }

    private void updateExecutionState(String stepName, FlowStepStatus status) {
        //log.info("步骤 {} 状态更新为: {}", stepName, status);
    }

}