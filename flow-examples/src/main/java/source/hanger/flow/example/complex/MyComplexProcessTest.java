package source.hanger.flow.example.complex;

import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.StepDefinition;
import source.hanger.flow.contract.model.Transition;
import source.hanger.flow.core.runtime.execution.FlowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess;
import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate;

/**
 * MyComplexProcess.groovy 完整测试
 *
 * 演示如何使用CompletableFlowEngine执行复杂流程定义
 *
 * @author fuhangbo.hanger.uhfun
 */
public class MyComplexProcessTest {

    private static final Logger logger = LoggerFactory.getLogger(MyComplexProcessTest.class);

    public static void main(String[] args) {
        MyComplexProcessTest test = new MyComplexProcessTest();

        // 运行基本测试
        test.runTest();

        logger.info("\n" + "=".repeat(50) + "\n");

        // 运行并发测试
        test.runConcurrentTest();
    }

    /**
     * 运行测试
     */
    public void runTest() {
        try {
            logger.info("=== 开始MyComplexProcess.groovy完整测试 ===");

            // 1. 创建复杂流程定义
            FlowDefinition flowDefinition = createMockComplexProcess();
            logger.info("流程定义创建完成，流程名称: {}", flowDefinition.getName());

            // 2. 创建流程引擎
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 3. 准备初始参数
            Map<String, Object> initialParams = new HashMap<>();
            initialParams.put("orderId", "ORDER_" + System.currentTimeMillis());
            initialParams.put("userEmail", "user@example.com");
            initialParams.put("userPhone", "13800138000");
            initialParams.put("userName", "测试用户");

            logger.info("开始执行复杂流程，订单ID: {}", initialParams.get("orderId"));

            // 4. 异步执行流程
            CompletableFuture<FlowResult> future = engine.execute(flowDefinition, initialParams);

            // 5. 等待执行完成并获取结果
            FlowResult result = future.get();

            // 6. 输出执行结果
            logger.info("复杂流程执行完成！");
            logger.info("执行状态: {}", result.getStatus());
            logger.info("执行ID: {}", result.getExecutionId());
            logger.info("最终输入: {}", result.getAttributes());

            if (result.isSuccess()) {
                logger.info("✅ 复杂流程测试成功！流程正常执行完成");
            } else {
                logger.error("❌ 复杂流程测试失败！执行状态: {}", result.getStatus());
                if (result.getError() != null) {
                    logger.error("错误详情: {}", result.getError());
                }
            }

            // 7. 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            logger.error("复杂流程测试执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建模拟的复杂流程定义
     */
    private FlowDefinition createMockComplexProcess() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("模拟订单处理与通知流程");
        flow.setDescription("模拟MyComplexProcess.groovy的复杂流程，包含订单初始化、库存检查、支付处理等");
        flow.setVersion("1.0.0");

        // 添加多个任务步骤
        addTaskStep(flow, "订单初始化", "准备订单数据，设置订单状态为待处理");
        addTaskStep(flow, "库存检查", "检查所有订单商品的库存是否充足");
        addTaskStep(flow, "支付处理", "调用支付网关完成支付");
        addTaskStep(flow, "通知库存不足", "通知用户商品库存不足，并终止订单流程");
        addTaskStep(flow, "通知支付失败", "通知用户支付失败，并引导重试或取消订单");
        addTaskStep(flow, "物流分配", "为订单分配物流渠道，生成物流单号");
        addTaskStep(flow, "拣货打包", "根据订单商品进行拣选和打包，更新库存");
        addTaskStep(flow, "发送订单确认邮件", "通过邮件向用户发送订单确认信息");
        addTaskStep(flow, "发送短信通知", "通过短信通知用户订单支付成功");
        addTaskStep(flow, "订单完成", "流程成功结束，订单状态最终完成并更新到数据库");
        addTaskStep(flow, "流程错误处理", "捕获全局或未处理的流程错误，并进行统一处理");
        addTaskStep(flow, "记录错误日志", "记录任务级别的错误日志，通常比全局错误更细致");
        addTaskStep(flow, "通知管理员", "通知相关管理员流程实例失败或出现异常");

        // 设置流转逻辑
        setupFlowTransitions(flow);

        return flow;
    }

    /**
     * 设置流程的流转逻辑
     */
    private void setupFlowTransitions(FlowDefinition flow) {
        // 获取所有步骤
        var steps = flow.getStepDefinitions();

        // 设置线性流转：订单初始化 -> 库存检查 -> 支付处理 -> 物流分配 -> 拣货打包 -> 发送订单确认邮件 -> 发送短信通知 -> 订单完成
        addTransition(steps.get(0), steps.get(1), "订单初始化 -> 库存检查"); // 订单初始化 -> 库存检查
        addTransition(steps.get(1), steps.get(2), "库存检查 -> 支付处理"); // 库存检查 -> 支付处理
        addTransition(steps.get(2), steps.get(5), "支付处理 -> 物流分配"); // 支付处理 -> 物流分配
        addTransition(steps.get(5), steps.get(6), "物流分配 -> 拣货打包"); // 物流分配 -> 拣货打包
        addTransition(steps.get(6), steps.get(7), "拣货打包 -> 发送订单确认邮件"); // 拣货打包 -> 发送订单确认邮件
        addTransition(steps.get(7), steps.get(8), "发送订单确认邮件 -> 发送短信通知"); // 发送订单确认邮件 -> 发送短信通知
        addTransition(steps.get(8), steps.get(9), "发送短信通知 -> 订单完成"); // 发送短信通知 -> 订单完成

        // 设置错误处理流转
        addTransition(steps.get(1), steps.get(3), "库存检查 -> 通知库存不足"); // 库存不足时
        addTransition(steps.get(2), steps.get(4), "支付处理 -> 通知支付失败"); // 支付失败时
        addTransition(steps.get(3), steps.get(10), "通知库存不足 -> 流程错误处理"); // 错误处理
        addTransition(steps.get(4), steps.get(10), "通知支付失败 -> 流程错误处理"); // 错误处理
        addTransition(steps.get(10), steps.get(11), "流程错误处理 -> 记录错误日志"); // 记录错误
        addTransition(steps.get(11), steps.get(12), "记录错误日志 -> 通知管理员"); // 通知管理员
    }

    /**
     * 添加流转关系
     */
    private void addTransition(StepDefinition from, StepDefinition to, String description) {
        // 创建默认条件（总是执行）
        FlowRuntimePredicate predicate = new FlowRuntimePredicate() {
            @Override
            public boolean test(FlowRuntimeExecuteAccess access) {
                // 默认总是流转
                access.log("执行流转: " + description);
                return true;
            }
        };

        // 创建Transition对象
        Transition transition = new Transition(predicate, to.getName());

        // 添加到步骤的流转列表中
        from.addTransition(transition);
    }

    /**
     * 添加任务步骤到流程中
     */
    private void addTaskStep(FlowDefinition flow, String name, String description) {
        source.hanger.flow.contract.model.TaskStepDefinition task
            = new source.hanger.flow.contract.model.TaskStepDefinition();
        task.setName(name);
        task.setDescription(description);

        // 设置任务执行逻辑
        task.setTaskRunnable(new source.hanger.flow.contract.runtime.common.FlowClosure() {
            @Override
            public void call(FlowRuntimeExecuteAccess access) {
                MyComplexProcessTest.logger.info("[TASK RUN] 执行任务: {}", name);

                // 模拟业务逻辑
                switch (name) {
                    case "订单初始化":
                        MyComplexProcessTest.logger.info("准备订单数据，设置订单状态为待处理");
                        break;
                    case "库存检查":
                        MyComplexProcessTest.logger.info("检查所有订单商品的库存是否充足");
                        break;
                    case "支付处理":
                        MyComplexProcessTest.logger.info("调用支付网关完成支付");
                        break;
                    case "通知库存不足":
                        MyComplexProcessTest.logger.info("通知用户商品库存不足，并终止订单流程");
                        break;
                    case "通知支付失败":
                        MyComplexProcessTest.logger.info("通知用户支付失败，并引导重试或取消订单");
                        break;
                    case "物流分配":
                        MyComplexProcessTest.logger.info("为订单分配物流渠道，生成物流单号");
                        break;
                    case "拣货打包":
                        MyComplexProcessTest.logger.info("根据订单商品进行拣选和打包，更新库存");
                        break;
                    case "发送订单确认邮件":
                        MyComplexProcessTest.logger.info("通过邮件向用户发送订单确认信息");
                        break;
                    case "发送短信通知":
                        MyComplexProcessTest.logger.info("通过短信通知用户订单支付成功");
                        break;
                    case "订单完成":
                        MyComplexProcessTest.logger.info("流程成功结束，订单状态最终完成并更新到数据库");
                        break;
                    case "流程错误处理":
                        MyComplexProcessTest.logger.info("捕获全局或未处理的流程错误，并进行统一处理");
                        break;
                    case "记录错误日志":
                        MyComplexProcessTest.logger.info("记录任务级别的错误日志，通常比全局错误更细致");
                        break;
                    case "通知管理员":
                        MyComplexProcessTest.logger.info("通知相关管理员流程实例失败或出现异常");
                        break;
                    default:
                        MyComplexProcessTest.logger.info("执行通用任务逻辑");
                }

                // 模拟处理时间
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                MyComplexProcessTest.logger.info("任务完成: {}", name);
                access.log("任务执行完成: " + name);
            }
        });

        flow.getStepDefinitions().add(task);
    }

    /**
     * 运行并发测试
     */
    public void runConcurrentTest() {
        try {
            logger.info("=== 开始MyComplexProcess并发测试 ===");

            // 创建复杂流程定义
            FlowDefinition flowDefinition = createMockComplexProcess();

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(8);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建多个并发任务
            CompletableFuture<FlowResult>[] futures = new CompletableFuture[3];

            for (int i = 0; i < 3; i++) {
                int orderIndex = i;

                Map<String, Object> initialParams = new HashMap<>();
                initialParams.put("orderId", "ORDER_CONCURRENT_" + orderIndex + "_" + System.currentTimeMillis());
                initialParams.put("userEmail", "user" + orderIndex + "@example.com");
                initialParams.put("userPhone", "1380013800" + orderIndex);
                initialParams.put("userName", "并发测试用户" + orderIndex);

                futures[i] = engine.execute(flowDefinition, initialParams)
                    .thenApply(result -> {
                        logger.info("并发任务 {} 完成，订单ID: {}", orderIndex, initialParams.get("orderId"));
                        return result;
                    });
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures).join();

            logger.info("所有并发任务执行完成！");

            // 输出结果
            for (int i = 0; i < futures.length; i++) {
                FlowResult result = futures[i].get();
                logger.info(
                    "任务 {} 结果: 状态={}, 执行ID={}",
                    i, result.getStatus(), result.getExecutionId());
            }

            executor.shutdown();

        } catch (Exception e) {
            logger.error("并发测试执行失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }
} 