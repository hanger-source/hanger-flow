package source.hanger.flow.engine;

import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.FlowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MyComplexProcess.groovy 完整测试
 *
 * 演示如何使用CompletableFlowEngine执行复杂流程定义
 *
 * @author fuhangbo.hanger.uhfun
 */
public class MyComplexProcessTest {

    private static final Logger log = LoggerFactory.getLogger(MyComplexProcessTest.class);

    public static void main(String[] args) {
        MyComplexProcessTest test = new MyComplexProcessTest();

        // 运行基本测试
        test.runTest();

        log.info("\n" + "=".repeat(50) + "\n");

        // 运行并发测试
        test.runConcurrentTest();
    }

    /**
     * 运行测试
     */
    public void runTest() {
        try {
            log.info("=== 开始MyComplexProcess.groovy完整测试 ===");

            // 1. 创建复杂流程定义
            FlowDefinition flowDefinition = createMockComplexProcess();
            log.info("流程定义创建完成，流程名称: {}", flowDefinition.getName());

            // 2. 创建流程引擎
            ExecutorService executor = Executors.newFixedThreadPool(4);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 3. 准备初始参数
            Map<String, Serializable> initialParams = new HashMap<>();
            initialParams.put("orderId", "ORDER_" + System.currentTimeMillis());
            initialParams.put("userEmail", "user@example.com");
            initialParams.put("userPhone", "13800138000");
            initialParams.put("userName", "测试用户");

            log.info("开始执行复杂流程，订单ID: {}", initialParams.get("orderId"));

            // 4. 异步执行流程
            CompletableFuture<FlowResult> future = engine.execute(flowDefinition, initialParams);

            // 5. 等待执行完成并获取结果
            FlowResult result = future.get();

            // 6. 输出执行结果
            log.info("复杂流程执行完成！");
            log.info("执行状态: {}", result.getStatus());
            log.info("执行ID: {}", result.getExecutionId());
            log.info("最终参数: {}", result.getParams());

            if (result.isSuccess()) {
                log.info("✅ 复杂流程测试成功！流程正常执行完成");
            } else {
                log.error("❌ 复杂流程测试失败！执行状态: {}", result.getStatus());
                if (result.getError() != null) {
                    log.error("错误详情: {}", result.getError());
                }
            }

            // 7. 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            log.error("复杂流程测试执行失败: {}", e.getMessage(), e);
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

        return flow;
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
        task.setTaskRunnable(new source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable() {
            @Override
            public void run(source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess access) {
                System.out.println("[TASK RUN] 执行任务: " + name);

                // 模拟业务逻辑
                switch (name) {
                    case "订单初始化":
                        System.out.println("准备订单数据，设置订单状态为待处理");
                        break;
                    case "库存检查":
                        System.out.println("检查所有订单商品的库存是否充足");
                        break;
                    case "支付处理":
                        System.out.println("调用支付网关完成支付");
                        break;
                    case "通知库存不足":
                        System.out.println("通知用户商品库存不足，并终止订单流程");
                        break;
                    case "通知支付失败":
                        System.out.println("通知用户支付失败，并引导重试或取消订单");
                        break;
                    case "物流分配":
                        System.out.println("为订单分配物流渠道，生成物流单号");
                        break;
                    case "拣货打包":
                        System.out.println("根据订单商品进行拣选和打包，更新库存");
                        break;
                    case "发送订单确认邮件":
                        System.out.println("通过邮件向用户发送订单确认信息");
                        break;
                    case "发送短信通知":
                        System.out.println("通过短信通知用户订单支付成功");
                        break;
                    case "订单完成":
                        System.out.println("流程成功结束，订单状态最终完成并更新到数据库");
                        break;
                    case "流程错误处理":
                        System.out.println("捕获全局或未处理的流程错误，并进行统一处理");
                        break;
                    case "记录错误日志":
                        System.out.println("记录任务级别的错误日志，通常比全局错误更细致");
                        break;
                    case "通知管理员":
                        System.out.println("通知相关管理员流程实例失败或出现异常");
                        break;
                    default:
                        System.out.println("执行通用任务逻辑");
                }

                // 模拟处理时间
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("任务完成: " + name);
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
            System.out.println("=== 开始MyComplexProcess并发测试 ===");

            // 创建复杂流程定义
            FlowDefinition flowDefinition = createMockComplexProcess();

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(8);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建多个并发任务
            CompletableFuture<FlowResult>[] futures = new CompletableFuture[3];

            for (int i = 0; i < 3; i++) {
                int orderIndex = i;

                Map<String, Serializable> initialParams = new HashMap<>();
                initialParams.put("orderId", "ORDER_CONCURRENT_" + orderIndex + "_" + System.currentTimeMillis());
                initialParams.put("userEmail", "user" + orderIndex + "@example.com");
                initialParams.put("userPhone", "1380013800" + orderIndex);
                initialParams.put("userName", "并发测试用户" + orderIndex);

                futures[i] = engine.execute(flowDefinition, initialParams)
                    .thenApply(result -> {
                        System.out.println("并发任务 " + orderIndex + " 完成，订单ID: " + initialParams.get("orderId"));
                        return result;
                    });
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures).join();

            System.out.println("所有并发任务执行完成！");

            // 输出结果
            for (int i = 0; i < futures.length; i++) {
                FlowResult result = futures[i].get();
                System.out.println(
                    "任务 " + i + " 结果: 状态=" + result.getStatus() + ", 执行ID=" + result.getExecutionId());
            }

            executor.shutdown();

        } catch (Exception e) {
            System.err.println("并发测试执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 