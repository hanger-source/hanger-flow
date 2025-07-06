package source.hanger.flow.example.simple;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.runtime.common.FlowClosure;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.status.FlowStatus;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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

    private static final Logger logger = LoggerFactory.getLogger(SimpleCompletableFlowTest.class);

    public static void main(String[] args) {
        SimpleCompletableFlowTest test = new SimpleCompletableFlowTest();
        test.runTest();
    }

    /**
     * 运行测试
     */
    public void runTest() {
        try {
            logger.info("=== 开始简单CompletableFuture流程引擎测试 ===");

            // 1. 创建流程定义
            FlowDefinition flowDefinition = createTestFlowDefinition();
            logger.info("创建测试流程定义: {}", flowDefinition.getName());

            // 2. 创建流程引擎
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // 3. 准备初始参数
            Map<String, Object> initialParams = new HashMap<>();
            initialParams.put("orderId", "TEST_ORDER_" + System.currentTimeMillis());
            initialParams.put("userName", "测试用户");
            initialParams.put("amount", 100.0);

            logger.info("开始执行测试流程，订单ID: {}", initialParams.get("orderId"));

            // 4. 手动执行流程（简化版本）
            FlowResult result = executeFlowManually(flowDefinition, initialParams);

            // 5. 输出执行结果
            logger.info("测试流程执行完成！");
            logger.info("执行状态: {}", result.getStatus());
            logger.info("执行ID: {}", result.getExecutionId());
            logger.info("最终输入: {}", result.getAttributes());

            if (result.isSuccess()) {
                logger.info("✅ 测试成功！流程正常执行完成");
            } else {
                logger.error("❌ 测试失败！执行状态: {}", result.getStatus());
                if (result.getError() != null) {
                    logger.error("错误详情: {}", result.getError());
                }
            }

            // 6. 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            logger.error("测试执行失败: {}", e.getMessage());
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
        testTask.setTaskRunnable(new FlowClosure() {
            @Override
            public void call(FlowRuntimeExecuteAccess access) {
                SimpleCompletableFlowTest.logger.info("[TASK RUN] 执行测试任务");

                // 模拟业务逻辑
                String orderId = "TEST_ORDER_" + System.currentTimeMillis();
                String userName = "测试用户";
                Double amount = 100.0;

                SimpleCompletableFlowTest.logger.info("处理订单: {}, 用户: {}, 金额: {}", orderId, userName, amount);

                // 模拟处理结果
                SimpleCompletableFlowTest.logger.info("测试任务执行完成");
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
    private FlowResult executeFlowManually(FlowDefinition flowDefinition, Map<String, Object> initialParams) {
        try {
            String executionId = flowDefinition.getName() + "_" + System.currentTimeMillis();
            logger.info("开始执行流程: {}, 执行ID: {}", flowDefinition.getName(), executionId);

            // 执行所有步骤
            for (source.hanger.flow.contract.model.StepDefinition step : flowDefinition.getStepDefinitions()) {
                if (step instanceof TaskStepDefinition) {
                    TaskStepDefinition taskStep = (TaskStepDefinition)step;
                    logger.info("执行任务步骤: {}", taskStep.getName());

                    // 执行任务
                    FlowClosure taskRunnable = taskStep.getTaskRunnable();
                    if (taskRunnable != null) {
                        taskRunnable.call(null);
                    }
                }
            }

            logger.info("流程执行完成: {}, 执行ID: {}", flowDefinition.getName(), executionId);
            return new FlowResult(executionId, FlowStatus.SUCCESS, initialParams);

        } catch (Exception e) {
            logger.error("流程执行异常: {}", flowDefinition.getName());
            e.printStackTrace();
            return new FlowResult("ERROR_" + System.currentTimeMillis(), FlowStatus.ERROR, initialParams, e);
        }
    }
} 