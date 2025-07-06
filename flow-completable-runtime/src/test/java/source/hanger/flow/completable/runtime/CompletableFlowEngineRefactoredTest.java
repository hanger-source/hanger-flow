package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.runtime.common.FlowClosure;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.status.FlowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CompletableFlowEngine重构后的回归测试
 * <p>
 * 验证重构后的引擎功能是否正常，包括：
 * - 步骤执行器分发
 * - 错误处理策略
 * - 生命周期处理器
 * - 访问器实现
 */
public class CompletableFlowEngineRefactoredTest {

    private static final Logger log = LoggerFactory.getLogger(CompletableFlowEngineRefactoredTest.class);

    public static void main(String[] args) {
        CompletableFlowEngineRefactoredTest test = new CompletableFlowEngineRefactoredTest();
        test.runBasicTest();
        test.runErrorHandlingTest();
        test.runLifecycleTest();
        test.runCustomHandlerTest();
    }

    /**
     * 基础功能测试
     */
    public void runBasicTest() {
        log.info("=== 基础功能测试 ===");

        try {
            // 创建引擎
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建测试流程
            FlowDefinition flow = createTestFlow();

            // 执行流程
            Map<String, Object> params = new HashMap<>();
            params.put("testParam", "testValue");

            CompletableFuture<FlowResult> future = engine.execute(flow, params);
            FlowResult result = future.get();

            // 验证结果
            if (result.isSuccess()) {
                log.info("✅ 基础功能测试通过");
            } else {
                log.error("❌ 基础功能测试失败: {}", result.getStatus());
            }

            executor.shutdown();

        } catch (Exception e) {
            log.error("基础功能测试异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 错误处理测试
     */
    public void runErrorHandlingTest() {
        log.info("\n=== 错误处理测试 ===");

        try {
            // 创建引擎
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建会抛出异常的测试流程
            FlowDefinition flow = createErrorTestFlow();

            // 执行流程
            Map<String, Object> params = new HashMap<>();
            params.put("testParam", "testValue");

            CompletableFuture<FlowResult> future = engine.execute(flow, params);
            FlowResult result = future.get();

            // 验证错误处理
            if (result.getStatus() == FlowStatus.ERROR) {
                log.info("✅ 错误处理测试通过");
            } else {
                log.error("❌ 错误处理测试失败: 期望ERROR状态，实际{}", result.getStatus());
            }

            executor.shutdown();

        } catch (Exception e) {
            log.error("错误处理测试异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 生命周期测试
     */
    public void runLifecycleTest() {
        log.info("\n=== 生命周期测试 ===");

        try {
            // 创建引擎
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建测试流程
            FlowDefinition flow = createTestFlow();

            // 执行流程
            Map<String, Object> params = new HashMap<>();
            params.put("testParam", "testValue");

            CompletableFuture<FlowResult> future = engine.execute(flow, params);
            FlowResult result = future.get();

            // 验证生命周期回调
            if (result.isSuccess()) {
                log.info("✅ 生命周期测试通过");
            } else {
                log.error("❌ 生命周期测试失败: {}", result.getStatus());
            }

            executor.shutdown();

        } catch (Exception e) {
            log.error("生命周期测试异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 自定义处理器测试
     */
    public void runCustomHandlerTest() {
        log.info("\n=== 自定义处理器测试 ===");

        try {
            // 创建引擎
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建会抛出异常的测试流程
            FlowDefinition flow = createErrorTestFlow();

            // 执行流程
            Map<String, Object> params = new HashMap<>();
            params.put("testParam", "testValue");

            CompletableFuture<FlowResult> future = engine.execute(flow, params);
            FlowResult result = future.get();

            // 验证自定义处理器
            if (result.getStatus() == FlowStatus.ERROR) {
                log.info("✅ 自定义处理器测试通过");
            } else {
                log.error("❌ 自定义处理器测试失败: 期望ERROR状态，实际{}", result.getStatus());
            }

            executor.shutdown();

        } catch (Exception e) {
            log.error("自定义处理器测试异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建测试流程
     */
    private FlowDefinition createTestFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("重构测试流程");
        flow.setDescription("用于验证重构后引擎功能的测试流程");
        flow.setVersion("1.0.0");

        // 添加任务步骤
        TaskStepDefinition task = new TaskStepDefinition();
        task.setName("测试任务");
        task.setDescription("测试任务步骤");
        task.setTaskRunnable(new FlowClosure() {
            @Override
            public void call(FlowRuntimeExecuteAccess access) {
                CompletableFlowEngineRefactoredTest.log.info("[TestTask] 执行测试任务");
                access.log("测试任务执行完成");
            }
        });

        flow.getStepDefinitions().add(task);
        return flow;
    }

    /**
     * 创建错误测试流程
     */
    private FlowDefinition createErrorTestFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("错误测试流程");
        flow.setDescription("用于验证错误处理功能的测试流程");
        flow.setVersion("1.0.0");

        // 添加会抛出异常的任务步骤
        TaskStepDefinition task = new TaskStepDefinition();
        task.setName("错误任务");
        task.setDescription("会抛出异常的任务步骤");
        task.setTaskRunnable(new FlowClosure() {
            @Override
            public void call(FlowRuntimeExecuteAccess access) {
                CompletableFlowEngineRefactoredTest.log.info("[ErrorTask] 执行错误任务");
                throw new RuntimeException("模拟任务执行异常");
            }
        });

        flow.getStepDefinitions().add(task);
        return flow;
    }
} 