package source.hanger.flow.example.dsl;

import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.contract.model.*;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;
import source.hanger.flow.contract.runtime.task.function.FlowTaskEnterHandingRunnable;
import source.hanger.flow.contract.runtime.task.function.FlowTaskErrorHandingRunnable;
import source.hanger.flow.contract.runtime.flow.function.FlowEnterHandingRunnable;
import source.hanger.flow.contract.runtime.flow.function.FlowErrorHandingRunnable;
import source.hanger.flow.contract.runtime.common.predicate.FlowRuntimePredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * DSL + CompletableFlow 全链路示例
 * <p>
 * 演示如何：
 *   - 从DSL脚本解析得到FlowDefinition
 *   - 使用CompletableFlowEngine执行流程
 *   - 完整的DSL → 解析 → 运行时执行链路
 */
public class DslCompletableFlowExample {

    private static final Logger log = LoggerFactory.getLogger(DslCompletableFlowExample.class);

    public static void main(String[] args) throws IOException {
        try {
            log.info("=== DSL + CompletableFlow 全链路测试 ===");
            
            // 1. 创建流程定义（模拟从DSL解析得到）
            FlowDefinition flowDefinition = createSampleFlowFromDsl();
            
            // 2. 创建CompletableFlow引擎
            CompletableFlowEngine engine = new CompletableFlowEngine();
            
            // 3. 执行流程
            log.info("开始执行流程...");
            CompletableFuture<FlowResult> future = engine.execute(flowDefinition);
            
            // 4. 等待完成并获取结果
            FlowResult result = future.get();
            
            // 5. 输出结果
            log.info("流程执行完成:");
            log.info("执行ID: {}", result.getExecutionId());
            log.info("状态: {}", result.getStatus());
            log.info("参数: {}", result.getParams());
            
            if (result.isError()) {
                log.error("错误: {}", result.getError());
            }
            
        } catch (Exception e) {
            log.error("流程执行异常: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建示例流程（模拟从DSL解析得到）
     * 这里暂时手动创建，实际应该从DSL解析器得到
     */
    private static FlowDefinition createSampleFlowFromDsl() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("DSL示例流程");
        flow.setDescription("演示DSL解析到CompletableFlow执行的全链路");
        flow.setVersion("1.0.0");
        
        // 设置流程进入回调
        flow.setEnterHandingRunnable(createFlowEnterHandler());
        
        // 设置流程错误处理回调
        flow.setErrorHandingRunnable(createFlowErrorHandler());
        
        // 创建任务步骤1
        TaskStepDefinition task1 = createTaskStep("task1", "第一个任务", createTask1Handler());
        flow.addStep(task1);
        
        // 创建并行步骤
        ParallelStepDefinition parallelStep = createParallelStep();
        flow.addStep(parallelStep);
        
        // 创建异步步骤
        AsyncStepDefinition asyncStep = createAsyncStep();
        flow.addStep(asyncStep);
        
        // 创建任务步骤2
        TaskStepDefinition task2 = createTaskStep("task2", "最后一个任务", createTask2Handler());
        flow.addStep(task2);
        
        return flow;
    }
    
    /**
     * 创建流程进入处理器
     */
    private static FlowEnterHandingRunnable createFlowEnterHandler() {
        return access -> {
            access.log("流程开始执行");
            log.info("流程进入: 开始执行DSL示例流程");
        };
    }
    
    /**
     * 创建流程错误处理器
     */
    private static FlowErrorHandingRunnable createFlowErrorHandler() {
        return access -> {
            access.log("流程执行出错: " + access.getException().getMessage());
            log.error("流程错误: {}", access.getException().getMessage());
        };
    }
    
    /**
     * 创建任务步骤
     */
    private static TaskStepDefinition createTaskStep(String name, String description, FlowTaskRunnable taskHandler) {
        TaskStepDefinition task = new TaskStepDefinition();
        task.setName(name);
        task.setDescription(description);
        task.setTaskRunnable(taskHandler);
        task.setEnterHandingRunnable(createTaskEnterHandler(name));
        task.setErrorHandingRunnable(createTaskErrorHandler(name));
        return task;
    }
    
    /**
     * 创建任务1处理器
     */
    private static FlowTaskRunnable createTask1Handler() {
        return access -> {
            access.log("执行任务1");
            log.info("执行任务1: 模拟业务逻辑");
            
            // 模拟一些工作
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            log.info("任务1完成");
        };
    }
    
    /**
     * 创建任务2处理器
     */
    private static FlowTaskRunnable createTask2Handler() {
        return access -> {
            access.log("执行任务2");
            log.info("执行任务2: 模拟业务逻辑");
            
            // 模拟一些工作
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            log.info("任务2完成");
        };
    }
    
    /**
     * 创建任务进入处理器
     */
    private static FlowTaskEnterHandingRunnable createTaskEnterHandler(String taskName) {
        return access -> {
            access.log("进入任务: " + taskName);
            log.info("进入任务: {}", taskName);
        };
    }
    
    /**
     * 创建任务错误处理器
     */
    private static FlowTaskErrorHandingRunnable createTaskErrorHandler(String taskName) {
        return access -> {
            access.log("任务出错: " + taskName + ", 错误: " + access.getException().getMessage());
            log.error("任务错误: {}, 错误: {}", taskName, access.getException().getMessage());
        };
    }
    
    /**
     * 创建并行步骤
     */
    private static ParallelStepDefinition createParallelStep() {
        ParallelStepDefinition parallel = new ParallelStepDefinition();
        parallel.setName("parallel");
        parallel.setDescription("并行处理步骤");
        
        // 添加分支1
        Branch branch1 = new Branch(createBranchCondition("branch1"), "task1");
        parallel.addBranch(branch1);
        
        // 添加分支2
        Branch branch2 = new Branch(createBranchCondition("branch2"), "task2");
        parallel.addBranch(branch2);
        
        // 设置汇合点
        parallel.setJoinBranchNames(java.util.Arrays.asList("task1", "task2"));
        
        return parallel;
    }
    
    /**
     * 创建分支条件
     */
    private static FlowRuntimePredicate createBranchCondition(String branchName) {
        return access -> {
            access.log("检查分支条件: " + branchName);
            log.info("检查分支条件: {}", branchName);
            // 这里可以根据上下文参数进行条件判断
            // 简化示例，总是返回true
            return true;
        };
    }
    
    /**
     * 创建异步步骤
     */
    private static AsyncStepDefinition createAsyncStep() {
        AsyncStepDefinition async = new AsyncStepDefinition();
        async.setName("async");
        async.setDescription("异步处理步骤");
        
        // 添加异步分支
        async.addBranchName("task1");
        async.addBranchName("task2");
        
        return async;
    }
} 