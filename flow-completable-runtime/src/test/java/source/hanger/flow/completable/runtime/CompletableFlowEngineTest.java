package source.hanger.flow.completable.runtime;

import org.junit.Test;
import source.hanger.flow.contract.model.*;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * CompletableFlowEngine单元测试
 */
public class CompletableFlowEngineTest {
    
    @Test
    public void testSimpleTaskExecution() throws ExecutionException, InterruptedException, TimeoutException {
        // 创建流程引擎
        CompletableFlowEngine engine = new CompletableFlowEngine();
        
        // 创建简单流程
        FlowDefinition flow = new FlowDefinition();
        flow.setName("测试流程");
        flow.setDescription("简单任务测试");
        
        // 创建任务步骤
        TaskStepDefinition task = new TaskStepDefinition();
        task.setName("testTask");
        task.setDescription("测试任务");
        task.setTaskRunnable(access -> {
            access.log("执行测试任务");
            // 模拟工作
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        flow.addStep(task);
        
        // 执行流程
        CompletableFuture<FlowResult> future = engine.execute(flow);
        FlowResult result = future.get(5, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(FlowStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getExecutionId());
    }
    
    @Test
    public void testParallelExecution() throws ExecutionException, InterruptedException, TimeoutException {
        // 创建流程引擎
        CompletableFlowEngine engine = new CompletableFlowEngine();
        
        // 创建流程
        FlowDefinition flow = new FlowDefinition();
        flow.setName("并行测试流程");
        
        // 创建任务1
        TaskStepDefinition task1 = new TaskStepDefinition();
        task1.setName("task1");
        task1.setTaskRunnable(createTaskHandler("task1", 200));
        flow.addStep(task1);
        
        // 创建任务2
        TaskStepDefinition task2 = new TaskStepDefinition();
        task2.setName("task2");
        task2.setTaskRunnable(createTaskHandler("task2", 300));
        flow.addStep(task2);
        
        // 创建并行步骤
        ParallelStepDefinition parallel = new ParallelStepDefinition();
        parallel.setName("parallel");
        parallel.setDescription("并行执行");
        
        // 添加分支
        Branch branch1 = new Branch(null, "task1");
        Branch branch2 = new Branch(null, "task2");
        parallel.addBranch(branch1);
        parallel.addBranch(branch2);
        
        // 设置汇合点
        parallel.setJoinBranchNames(java.util.Arrays.asList("task1", "task2"));
        flow.addStep(parallel);
        
        // 执行流程
        CompletableFuture<FlowResult> future = engine.execute(flow);
        FlowResult result = future.get(10, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(FlowStatus.SUCCESS, result.getStatus());
    }
    
    @Test
    public void testAsyncExecution() throws ExecutionException, InterruptedException, TimeoutException {
        // 创建流程引擎
        CompletableFlowEngine engine = new CompletableFlowEngine();
        
        // 创建流程
        FlowDefinition flow = new FlowDefinition();
        flow.setName("异步测试流程");
        
        // 创建异步步骤
        AsyncStepDefinition async = new AsyncStepDefinition();
        async.setName("async");
        async.setDescription("异步执行");
        async.addBranchName("task1");
        async.addBranchName("task2");
        flow.addStep(async);
        
        // 创建任务1
        TaskStepDefinition task1 = new TaskStepDefinition();
        task1.setName("task1");
        task1.setTaskRunnable(createTaskHandler("task1", 100));
        flow.addStep(task1);
        
        // 创建任务2
        TaskStepDefinition task2 = new TaskStepDefinition();
        task2.setName("task2");
        task2.setTaskRunnable(createTaskHandler("task2", 150));
        flow.addStep(task2);
        
        // 执行流程
        CompletableFuture<FlowResult> future = engine.execute(flow);
        FlowResult result = future.get(5, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(FlowStatus.SUCCESS, result.getStatus());
    }
    
    @Test
    public void testErrorHandling() throws ExecutionException, InterruptedException, TimeoutException {
        // 创建流程引擎
        CompletableFlowEngine engine = new CompletableFlowEngine();
        
        // 创建流程
        FlowDefinition flow = new FlowDefinition();
        flow.setName("错误处理测试流程");
        
        // 创建会抛出异常的任务
        TaskStepDefinition errorTask = new TaskStepDefinition();
        errorTask.setName("errorTask");
        errorTask.setTaskRunnable(access -> {
            access.log("执行会出错的任务");
            throw new RuntimeException("模拟任务执行错误");
        });
        errorTask.setErrorHandingRunnable(access -> {
            access.log("处理任务错误: " + access.getException().getMessage());
        });
        
        flow.addStep(errorTask);
        
        // 执行流程
        CompletableFuture<FlowResult> future = engine.execute(flow);
        FlowResult result = future.get(5, TimeUnit.SECONDS);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(FlowStatus.ERROR, result.getStatus());
        assertNotNull(result.getError());
    }
    
    /**
     * 创建任务处理器
     */
    private FlowTaskRunnable createTaskHandler(String taskName, long sleepTime) {
        return access -> {
            access.log("执行任务: " + taskName);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            access.log("任务完成: " + taskName);
        };
    }
} 