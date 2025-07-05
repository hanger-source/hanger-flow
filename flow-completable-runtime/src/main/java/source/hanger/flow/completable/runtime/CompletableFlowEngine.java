package source.hanger.flow.completable.runtime;

import source.hanger.flow.completable.runtime.access.CompletableFlowTaskRunAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowTaskRunAccessContext;
import source.hanger.flow.contract.model.*;
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicate;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;
import source.hanger.flow.core.runtime.FlowLifecycleHandler;
import source.hanger.flow.core.runtime.StepLifecycleHandler;
import source.hanger.flow.completable.runtime.lifecycle.DefaultFlowLifecycleHandler;
import source.hanger.flow.completable.runtime.lifecycle.DefaultStepLifecycleHandler;
import source.hanger.flow.core.util.FlowLogger;
import source.hanger.flow.core.runtime.FlowExecutionContext;
import source.hanger.flow.core.runtime.FlowExecutionState;
import source.hanger.flow.core.runtime.FlowExecutionManager;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;
import source.hanger.flow.core.runtime.FlowStepStatus;
import source.hanger.flow.core.runtime.StepExecutor;
import source.hanger.flow.core.runtime.StepErrorHandler;
import source.hanger.flow.core.runtime.FlowErrorHandler;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimePredicateAccess;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimePredicateAccessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.logging.Level;

/**
 * CompletableFuture流程引擎
 * <p>
 * 作用：
 * - 基于CompletableFuture实现异步流程执行
 * - 支持任务、并行、异步分支等所有流程节点类型
 * - 提供完整的错误处理和生命周期管理
 * <p>
 * 设计特点：
 * - 完全异步执行，支持高并发
 * - 基于CompletableFuture的链式调用
 * - 支持流程状态跟踪和监控
 * - 提供丰富的执行上下文和访问接口
 * <p>
 * 典型用法：
 * FlowDefinition flowDef = ...; // 从DSL解析得到
 * CompletableFlowEngine engine = new CompletableFlowEngine();
 * CompletableFuture<FlowResult> future = engine.execute(flowDef);
 * FlowResult result = future.get(); // 等待完成
 */
public class CompletableFlowEngine {

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
     * 流程执行结果缓存
     */
    private final Map<String, FlowResult> resultCache = new ConcurrentHashMap<>();

    /**
     * 步骤类型到执行器的映射
     */
    private final Map<Class<?>, StepExecutor> stepExecutors = new ConcurrentHashMap<>();
    /**
     * 流程执行管理器
     */
    private final FlowExecutionManager executionManager = new FlowExecutionManager();
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
        stepExecutors.put(TaskStepDefinition.class, new TaskStepExecutor());
        stepExecutors.put(ParallelStepDefinition.class, new ParallelStepExecutor());
        stepExecutors.put(AsyncStepDefinition.class, new AsyncStepExecutor());
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
        stepExecutors.put(TaskStepDefinition.class, new TaskStepExecutor());
        stepExecutors.put(ParallelStepDefinition.class, new ParallelStepExecutor());
        stepExecutors.put(AsyncStepDefinition.class, new AsyncStepExecutor());
    }

    /**
     * 执行流程
     *
     * @param flowDefinition 流程定义
     * @return 流程执行结果
     */
    public CompletableFuture<FlowResult> execute(FlowDefinition flowDefinition) {
        return execute(flowDefinition, new HashMap<>());
    }

    /**
     * 执行流程（带初始参数）
     *
     * @param flowDefinition 流程定义
     * @param initialParams  初始参数
     * @return 流程执行结果
     */
    public CompletableFuture<FlowResult> execute(FlowDefinition flowDefinition,
        Map<String, Serializable> initialParams) {
        String executionId = generateExecutionId(flowDefinition);
        FlowExecutionContext context = new FlowExecutionContext(executionId, flowDefinition, initialParams);

        FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "流程开始执行"), "🚀 流程开始执行");

        return CompletableFuture.supplyAsync(() -> {
            try {
                // 执行流程进入回调
                executeFlowEnter(flowDefinition, context);

                // 执行所有步骤
                FlowResult result = executeSteps(flowDefinition, context);

                FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "流程执行完成"), "🎉 流程执行完成");
                return result;

            } catch (Exception e) {
                FlowLogger.log(FlowLogger.Level.ERROR, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "流程执行错误: " + e.getMessage()), "💥 流程执行错误: " + e.getMessage());
                return handleFlowError(flowDefinition, context, e);
            }
        }, executor);
    }

    /**
     * 执行流程进入回调
     */
    private void executeFlowEnter(FlowDefinition flowDefinition, FlowExecutionContext context) {
        flowLifecycleHandler.onFlowStart(flowDefinition, context);
    }

    /**
     * 执行所有步骤
     */
    private FlowResult executeSteps(FlowDefinition flowDefinition, FlowExecutionContext context) {
        List<StepDefinition> steps = flowDefinition.getStepDefinitions();
        if (steps == null || steps.isEmpty()) {
            return new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams());
        }

        // 用stream查找 __START__ 节点
        StepDefinition startStep = steps.stream()
            .filter(step -> "__START__".equals(step.getName()))
            .findFirst()
            .orElse(steps.get(0));

        // __START__ 节点的第一个Transition的目标为实际业务第一个节点
        StepDefinition firstStep = startStep;
        if ("__START__".equals(startStep.getName()) && startStep instanceof AbstractStepDefinition) {
            List<Transition> transitions = ((AbstractStepDefinition)startStep).getTransitions();
            if (transitions != null && !transitions.isEmpty()) {
                String nextStepName = transitions.get(0).nextStepName();
                firstStep = steps.stream()
                    .filter(step -> nextStepName.equals(step.getName()))
                    .findFirst()
                    .orElse(startStep);
            }
        }

        // 创建步骤执行器
        InternalStepExecutor stepExecutor = new InternalStepExecutor(context, executor);
        CompletableFuture<FlowResult> future = stepExecutor.executeStep(firstStep);
        // 等待所有步骤完成
        return future.join();
    }

    /**
     * 处理流程错误
     */
    private FlowResult handleFlowError(FlowDefinition flowDefinition, FlowExecutionContext context, Exception error) {
        flowLifecycleHandler.onFlowError(flowDefinition, context, error);
        FlowResult result = flowErrorHandler.handleError(flowDefinition, context, error);
        return result != null
            ? result
            : new FlowResult(context.getExecutionId(), FlowStatus.ERROR, context.getParams(), error);
    }

    /**
     * 生成执行ID
     */
    private String generateExecutionId(FlowDefinition flowDefinition) {
        return flowDefinition.getName() + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * 获取执行状态
     */
    public FlowExecutionState getExecutionState(String executionId) {
        return executionManager.getExecutionState(executionId);
    }

    /**
     * 获取执行结果
     */
    public FlowResult getExecutionResult(String executionId) {
        return executionManager.getExecutionResult(executionId);
    }

    /**
     * 步骤执行器
     */
    private class InternalStepExecutor {
        private final FlowExecutionContext context;
        private final Executor executor;
        private final Map<String, CompletableFuture<FlowResult>> stepFutures = new ConcurrentHashMap<>();

        public InternalStepExecutor(FlowExecutionContext context, Executor executor) {
            this.context = context;
            this.executor = executor;
        }

        /**
         * 执行单个步骤
         */
        public CompletableFuture<FlowResult> executeStep(StepDefinition step) {
            String stepName = step.getName();
            String stepType = getStepType(step);
            String executionId = context.getExecutionId();

            FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "步骤开始执行"), "▶️ 步骤开始执行");

            // 步骤开始生命周期
            stepLifecycleHandler.onStepStart(step, context);

            // 优先通过stepExecutors分发
            StepExecutor executorImpl = stepExecutors.get(step.getClass());
            if (executorImpl != null) {
                // 只处理Task节点，其他类型暂时走原有逻辑
                return executorImpl.execute(step, context, executor)
                    .thenApply(result -> {
                        updateExecutionState(stepName, FlowStepStatus.COMPLETED);
                        FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "步骤执行完成"), "✅ 步骤执行完成");
                        // 步骤完成生命周期
                        stepLifecycleHandler.onStepComplete(step, context);
                        return executeNextSteps(step, result);
                    })
                    .exceptionally(e -> {
                        FlowLogger.log(FlowLogger.Level.ERROR, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "步骤执行错误 : " + e.getMessage()), "❌ 步骤执行错误 : " + e.getMessage());
                        updateExecutionState(stepName, FlowStepStatus.ERROR);
                        // 步骤错误生命周期
                        stepLifecycleHandler.onStepError(step, context,
                            e instanceof Exception ? (Exception)e : new RuntimeException(e));
                        return handleStepError(step, e instanceof Exception ? (Exception)e : new RuntimeException(e));
                    });
            }
            // 其他类型暂时走原有逻辑
            return CompletableFuture.supplyAsync(() -> {
                try {
                    updateExecutionState(stepName, FlowStepStatus.RUNNING);
                    FlowResult result = executeStepByType(step);
                    updateExecutionState(stepName, FlowStepStatus.COMPLETED);
                    FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "步骤执行完成"), "✅ 步骤执行完成");
                    // 步骤完成生命周期
                    stepLifecycleHandler.onStepComplete(step, context);
                    return executeNextSteps(step, result);
                } catch (Exception e) {
                    FlowLogger.log(FlowLogger.Level.ERROR, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "步骤执行错误 : " + e.getMessage()), "❌ 步骤执行错误 : " + e.getMessage());
                    updateExecutionState(stepName, FlowStepStatus.ERROR);
                    // 步骤错误生命周期
                    stepLifecycleHandler.onStepError(step, context, e);
                    return handleStepError(step, e);
                }
            }, executor);
        }

        /**
         * 获取步骤类型名称
         */
        private String getStepType(StepDefinition step) {
            if (step instanceof TaskStepDefinition) {
                return "TASK";
            } else if (step instanceof ParallelStepDefinition) {
                return "PARALLEL";
            } else if (step instanceof AsyncStepDefinition) {
                return "ASYNC";
            } else {
                return "UNKNOWN";
            }
        }

        /**
         * 根据步骤类型执行
         */
        private FlowResult executeStepByType(StepDefinition step) {
            if (step instanceof TaskStepDefinition) {
                return executeTaskStep((TaskStepDefinition)step);
            } else if (step instanceof ParallelStepDefinition) {
                return executeParallelStep((ParallelStepDefinition)step);
            } else if (step instanceof AsyncStepDefinition) {
                return executeAsyncStep((AsyncStepDefinition)step);
            } else {
                throw new UnsupportedOperationException("不支持的步骤类型: " + step.getClass().getSimpleName());
            }
        }

        /**
         * 执行任务步骤
         */
        private FlowResult executeTaskStep(TaskStepDefinition taskStep) {
            String stepName = taskStep.getName();

            // 执行任务进入回调
            executeTaskEnter(taskStep);

            // 执行任务主体
            FlowTaskRunnable taskRunnable = taskStep.getTaskRunnable();
            if (taskRunnable != null) {
                // 创建正确的访问上下文
                CompletableFlowTaskRunAccessContext accessContext
                    = new CompletableFlowTaskRunAccessContext(context, stepName);
                CompletableFlowTaskRunAccess access = new CompletableFlowTaskRunAccess(accessContext);
                taskRunnable.run(access);
            }

            return new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams());
        }

        /**
         * 执行并行步骤
         */
        private FlowResult executeParallelStep(ParallelStepDefinition parallelStep) {
            String stepName = parallelStep.getName();
            FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "执行并行步骤: " + stepName), "执行并行步骤: " + stepName);

            // 收集所有分支的Future
            Map<String, CompletableFuture<FlowResult>> branchFutures = new HashMap<>();

            // 执行所有分支
            Map<String, Branch> branches = parallelStep.getBranches();
            for (Map.Entry<String, Branch> entry : branches.entrySet()) {
                String targetStepName = entry.getKey();
                Branch branch = entry.getValue();
                FlowRuntimePredicate predicate = branch.flowRuntimePredicate();

                // 执行分支条件
                boolean condition = false;
                if (predicate != null) {
                    CompletableFlowRuntimePredicateAccessContext predicateContext
                        = new CompletableFlowRuntimePredicateAccessContext(context);
                    CompletableFlowRuntimePredicateAccess predicateAccess = new CompletableFlowRuntimePredicateAccess(
                        predicateContext);
                    condition = predicate.test(predicateAccess);
                } else {
                    condition = true;
                }
                if (condition) {
                    CompletableFuture<FlowResult> branchFuture = executeStepByName(targetStepName);
                    branchFutures.put(targetStepName, branchFuture);
                }
            }

            // 等待所有分支完成
            CompletableFuture.allOf(branchFutures.values().toArray(new CompletableFuture[0])).join();

            return new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams());
        }

        /**
         * 执行异步步骤
         */
        private FlowResult executeAsyncStep(AsyncStepDefinition asyncStep) {
            String stepName = asyncStep.getName();
            FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "执行异步步骤: " + stepName), "执行异步步骤: " + stepName);

            // 异步执行分支，不等待完成
            List<String> branchNames = asyncStep.getBranchNames();
            for (String branchName : branchNames) {
                CompletableFuture.runAsync(() -> {
                    try {
                        executeStepByName(branchName).join();
                    } catch (Exception e) {
                        FlowLogger.log(FlowLogger.Level.ERROR, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "异步分支执行异常: " + e.getMessage()), "异步分支执行异常: " + e.getMessage());
                    }
                }, executor);
            }

            return new FlowResult(context.getExecutionId(), FlowStatus.SUCCESS, context.getParams());
        }

        /**
         * 执行任务进入回调
         */
        private void executeTaskEnter(TaskStepDefinition taskStep) {
            FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "[TASK ENTER] 任务进入回调: " + taskStep.getName()), "[TASK ENTER] 任务进入回调: " + taskStep.getName());
        }

        /**
         * 处理步骤错误
         */
        private FlowResult handleStepError(StepDefinition step, Exception error) {
            return stepErrorHandler.handleError(step, context, error);
        }

        /**
         * 执行后续步骤
         */
        private FlowResult executeNextSteps(StepDefinition currentStep, FlowResult currentResult) {
            // 1. 获取当前节点的 transitions
            List<Transition> transitions = null;
            try {
                var method = currentStep.getClass().getMethod("getTransitions");
                transitions = (List<Transition>) method.invoke(currentStep);
            } catch (Exception e) {
                // 没有transitions方法，说明是终止节点
                return currentResult;
            }
            if (transitions == null || transitions.isEmpty()) {
                return currentResult;
            }
            // 2. 遍历所有transition，找到第一个条件成立的下一个节点
            for (Transition t : transitions) {
                boolean matched = true;
                String predicateDesc = (t.flowRuntimePredicate() != null) ? t.flowRuntimePredicate().getClass().getSimpleName() : "ALWAYS_TRUE";
                if (t.flowRuntimePredicate() != null) {
                    try {
                        CompletableFlowRuntimePredicateAccessContext predicateContext = new CompletableFlowRuntimePredicateAccessContext(context);
                        CompletableFlowRuntimePredicateAccess predicateAccess = new CompletableFlowRuntimePredicateAccess(predicateContext);
                        matched = t.flowRuntimePredicate().test(predicateAccess);
                    } catch (Exception ex) {
                        matched = false;
                    }
                }
                FlowLogger.log(FlowLogger.Level.INFO, new FlowLogger.FlowLogContext(context.getFlowDefinition().getName(), context.getFlowDefinition().getVersion(), context.getExecutionId(), "Transition] nextStep='" + t.nextStepName() + "', predicate='" + predicateDesc + "', matched=" + matched), "[Transition] nextStep='" + t.nextStepName() + "', predicate='" + predicateDesc + "', matched=" + matched);
                if (matched) {
                    String nextStepName = t.nextStepName();
                    if ("__END__".equals(nextStepName)) {
                        return currentResult;
                    }
                    StepDefinition nextStep = findStepByName(nextStepName);
                    if (nextStep != null) {
                        // 递归推进到下一个节点
                        return executeStep(nextStep).join();
                    }
                }
            }
            // 没有可用的transition，流程结束
            return currentResult;
        }

        /**
         * 根据步骤名称执行步骤
         */
        private CompletableFuture<FlowResult> executeStepByName(String stepName) {
            StepDefinition step = findStepByName(stepName);
            if (step != null) {
                return executeStep(step);
            } else {
                throw new IllegalArgumentException("找不到步骤: " + stepName);
            }
        }

        /**
         * 根据名称查找步骤
         */
        private StepDefinition findStepByName(String stepName) {
            List<StepDefinition> steps = context.getFlowDefinition().getStepDefinitions();
            for (StepDefinition step : steps) {
                if (stepName.equals(step.getName())) {
                    return step;
                }
            }
            return null;
        }

        /**
         * 查找下一个步骤
         */
        private StepDefinition findNextStep(StepDefinition currentStep) {
            List<StepDefinition> steps = context.getFlowDefinition().getStepDefinitions();
            int currentIndex = -1;
            for (int i = 0; i < steps.size(); i++) {
                if (steps.get(i) == currentStep) {
                    currentIndex = i;
                    break;
                }
            }
            if (currentIndex >= 0 && currentIndex < steps.size() - 1) {
                return steps.get(currentIndex + 1);
            }
            return null;
        }

        /**
         * 更新执行状态
         */
        private void updateExecutionState(String stepName, FlowStepStatus status) {
            executionManager.updateStepStatus(context.getExecutionId(), context.getFlowDefinition().getName(), stepName,
                status);
        }
    }
} 