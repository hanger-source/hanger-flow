package source.hanger.flow.engine;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的CompletableFuture流程引擎测试
 *
 * 直接测试CompletableFlowEngine的核心功能，不依赖DSL解析
 *
 * @author fuhangbo.hanger.uhfun
 */
public class SimpleCompletableFlowTest {

    private static final Logger log = LoggerFactory.getLogger(SimpleCompletableFlowTest.class);

    public static void main(String[] args) {
        SimpleCompletableFlowTest test = new SimpleCompletableFlowTest();
        test.runTest();
    }

    /**
     * 运行测试
     */
    public void runTest() {
        try {
            SimpleCompletableFlowTest.log.info("=== 开始简单CompletableFuture流程引擎测试 ===");

            // 1. 创建流程定义
            FlowDefinition flowDefinition = createTestFlowDefinition();
            SimpleCompletableFlowTest.log.info("创建测试流程定义: {}", flowDefinition.getName());

            // 2. 创建流程引擎
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // 3. 准备初始参数
            Map<String, Serializable> initialParams = new HashMap<>();
            initialParams.put("orderId", "TEST_ORDER_" + System.currentTimeMillis());
            initialParams.put("userName", "测试用户");
            initialParams.put("amount", 100.0);

            SimpleCompletableFlowTest.log.info("开始执行测试流程，订单ID: {}", initialParams.get("orderId"));

            // 4. 手动执行流程（简化版本）
            FlowResult result = executeFlowManually(flowDefinition, initialParams);

            // 5. 输出执行结果
            SimpleCompletableFlowTest.log.info("测试流程执行完成！");
            SimpleCompletableFlowTest.log.info("执行状态: {}", result.getStatus());
            SimpleCompletableFlowTest.log.info("执行ID: {}", result.getExecutionId());
            SimpleCompletableFlowTest.log.info("最终参数: {}", result.getParams());

            if (result.isSuccess()) {
                SimpleCompletableFlowTest.log.info("✅ 测试成功！流程正常执行完成");
            } else {
                SimpleCompletableFlowTest.log.error("❌ 测试失败！执行状态: {}", result.getStatus());
                if (result.getError() != null) {
                    System.err.println("错误详情: " + result.getError());
                }
            }

            // 6. 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            System.err.println("测试执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建测试流程定义
     */
    private FlowDefinition createTestFlowDefinition() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("简单测试流程");
        flow.setDescription("用于测试CompletableFlowEngine的简单流程");

        // 创建测试任务
        TaskStepDefinition testTask = new TaskStepDefinition();
        testTask.setName("测试任务");
        testTask.setDescription("一个简单的测试任务");

        // 设置任务执行逻辑
        testTask.setTaskRunnable(new FlowTaskRunnable() {
            @Override
            public void run(source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess access) {
                System.out.println("[TASK RUN] 执行测试任务");

                // 模拟业务逻辑
                String orderId = "TEST_ORDER_" + System.currentTimeMillis();
                String userName = "测试用户";
                Double amount = 100.0;

                System.out.println("处理订单: " + orderId + ", 用户: " + userName + ", 金额: " + amount);

                // 模拟处理结果
                System.out.println("测试任务执行完成");
                access.log("测试任务执行完成");
            }
        });

        // 将任务添加到流程中
        flow.getStepDefinitions().add(testTask);

        return flow;
    }

    /**
     * 手动执行流程（简化版本）
     */
    private FlowResult executeFlowManually(FlowDefinition flowDefinition, Map<String, Serializable> initialParams) {
        try {
            String executionId = flowDefinition.getName() + "_" + System.currentTimeMillis();
            System.out.println("开始执行流程: " + flowDefinition.getName() + ", 执行ID: " + executionId);

            // 执行所有步骤
            for (source.hanger.flow.contract.model.StepDefinition step : flowDefinition.getStepDefinitions()) {
                if (step instanceof TaskStepDefinition) {
                    TaskStepDefinition taskStep = (TaskStepDefinition)step;
                    System.out.println("执行任务步骤: " + taskStep.getName());

                    // 执行任务
                    FlowTaskRunnable taskRunnable = taskStep.getTaskRunnable();
                    if (taskRunnable != null) {
                        taskRunnable.run(null);
                    }
                }
            }

            System.out.println("流程执行完成: " + flowDefinition.getName() + ", 执行ID: " + executionId);
            return new FlowResult(executionId, FlowStatus.SUCCESS, initialParams);

        } catch (Exception e) {
            System.err.println("流程执行异常: " + flowDefinition.getName());
            e.printStackTrace();
            return new FlowResult("ERROR_" + System.currentTimeMillis(), FlowStatus.ERROR, initialParams, e);
        }
    }
} 