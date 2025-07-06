package source.hanger.flow.example.logging;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.model.ParallelStepDefinition;
import source.hanger.flow.contract.model.AsyncStepDefinition;
import source.hanger.flow.contract.model.Branch;
import source.hanger.flow.contract.runtime.common.FlowClosure;
import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate;
import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 增强日志功能示例
 * <p>
 * 展示新的日志系统如何提供更清晰的执行信息，
 * 包括时间戳、执行ID、步骤类型、执行状态等。
 */
public class EnhancedLoggingExample {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedLoggingExample.class);

    public static void main(String[] args) {
        logger.info("=== 增强日志功能示例 ===\n");

        // 创建线程池
        Executor executor = Executors.newFixedThreadPool(4);

        // 创建流程引擎
        CompletableFlowEngine engine = new CompletableFlowEngine(executor);

        // 构建复杂流程
        FlowDefinition flow = EnhancedLoggingExample.buildComplexFlow();

        // 执行流程
        CompletableFuture<Void> future = engine.execute(flow)
            .thenAccept(result -> {
                logger.info("\n=== 执行结果 ===");
                logger.info("状态: {}", result.getStatus());
                logger.info("输入: {}", result.getAttributes());
                if (result.getError() != null) {
                    logger.error("错误: {}", result.getError().getMessage(), result.getError());
                }
            });

        // 等待完成
        future.join();
    }

    /**
     * 构建包含多种步骤类型的复杂流程
     */
    private static FlowDefinition buildComplexFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("EnhancedLoggingFlow");

        // 任务步骤1
        TaskStepDefinition task1 = new TaskStepDefinition();
        task1.setName("初始化任务");
        task1.setTaskRunnable(new FlowClosure() {
            @Override
            public void call(FlowRuntimeExecuteAccess access) {
                try {
                    Thread.sleep(100); // 模拟任务执行
                    EnhancedLoggingExample.logger.info("    [任务执行] 初始化系统...");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // 并行步骤
        ParallelStepDefinition parallelStep = new ParallelStepDefinition();
        parallelStep.setName("并行处理");

        // 分支1
        Branch branch1 = new Branch(
            new FlowRuntimePredicate() {
                @Override
                public boolean test(FlowRuntimeExecuteAccess access) {
                    return true; // 总是执行
                }
            },
            "分支A"
        );

        // 分支2
        Branch branch2 = new Branch(
            new FlowRuntimePredicate() {
                @Override
                public boolean test(FlowRuntimeExecuteAccess access) {
                    return true; // 总是执行
                }
            },
            "分支B"
        );

        parallelStep.addBranch(branch1);
        parallelStep.addBranch(branch2);

        // 异步步骤
        AsyncStepDefinition asyncStep = new AsyncStepDefinition();
        asyncStep.setName("异步处理");
        asyncStep.addBranchName("异步分支1");
        asyncStep.addBranchName("异步分支2");

        // 任务步骤2
        TaskStepDefinition task2 = new TaskStepDefinition();
        task2.setName("最终任务");
        task2.setTaskRunnable(new FlowClosure() {
            @Override
            public void call(FlowRuntimeExecuteAccess access) {
                try {
                    Thread.sleep(50); // 模拟任务执行
                    EnhancedLoggingExample.logger.info("    [任务执行] 完成最终处理...");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // 组装流程
        flow.addStep(task1);
        flow.addStep(parallelStep);
        flow.addStep(asyncStep);
        flow.addStep(task2);

        return flow;
    }
} 