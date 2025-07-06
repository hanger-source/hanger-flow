package source.hanger.flow.example.channel;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.execution.FlowResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Channel复杂流程示例
 *
 * 演示如何使用CompletableFlowEngine执行带channel buffer通信的复杂流程
 * 展示步骤间的数据推送、监听、等待完整数据等功能
 *
 * @author fuhangbo.hanger.uhfun
 */
public class ChannelComplexFlowExample {

    private static final Logger logger = LoggerFactory.getLogger(ChannelComplexFlowExample.class);

    public static void main(String[] args) {
        ChannelComplexFlowExample example = new ChannelComplexFlowExample();
        example.runExample();
    }

    /**
     * 运行Channel复杂流程示例
     */
    public void runExample() {
        try {
            logger.info("=== 开始Channel复杂流程示例 ===");

            // 1. 解析DSL脚本
            FlowDefinition flowDefinition = parseDslScript();
            logger.info("DSL脚本解析完成，流程名称: {}", flowDefinition.getName());

            // 打印所有step及其transition，辅助排查
            if (flowDefinition.getStepDefinitions() != null) {
                for (var step : flowDefinition.getStepDefinitions()) {
                    Object transitions;
                    try {
                        var method = step.getClass().getMethod("getTransitions");
                        transitions = method.invoke(step);
                    } catch (Exception e) {
                        transitions = "N/A";
                    }
                    logger.info("[DEBUG] Step: {} transitions: {}", step.getName(), transitions);
                }
            }

            // 2. 创建流程引擎
            ExecutorService executor = new ThreadPoolExecutor(8, 8, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 3. 准备初始参数
            Map<String, Object> initialParams = new HashMap<>();
            initialParams.put("orderId", "CHANNEL_ORDER_" + System.currentTimeMillis());
            initialParams.put("userEmail", "channel@example.com");
            initialParams.put("userPhone", "13900139000");

            logger.info("开始执行Channel复杂流程，订单ID: {}", initialParams.get("orderId"));

            // 4. 异步执行流程
            CompletableFuture<FlowResult> future = engine.execute(flowDefinition, initialParams);

            // 5. 等待执行完成并获取结果
            FlowResult result = future.get();

            // 6. 输出执行结果
            logger.info("Channel复杂流程执行完成！");
            logger.info("执行状态: {}", result.getStatus());
            logger.info("执行ID: {}", result.getExecutionId());
            logger.info("最终输入: {}", result.getAttributes());
            if (result.isError()) {
                logger.error("执行失败，错误: {}", result.getError());
            }

            // 7. 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            logger.error("Channel复杂流程示例执行失败", e);
        }
    }

    /**
     * 解析DSL脚本
     *
     * @return 流程定义
     * @throws Exception 解析异常
     */
    private FlowDefinition parseDslScript() throws Exception {
        // 必须在 hanger-flow目录下 运行：mvn exec:java -Dexec.mainClass="source.hanger.flow.example.channel
        // .ChannelComplexFlowExample"
        String scriptPath = "flow-examples/src/main/resources/script/ChannelComplexProcess.groovy";
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            scriptPath = "src/main/resources/script/ChannelComplexProcess.groovy";
            scriptFile = new File(scriptPath);
        }

        if (!scriptFile.exists()) {
            throw new RuntimeException("找不到Channel复杂流程DSL脚本文件: " + scriptPath);
        }

        logger.info("开始解析Channel复杂流程DSL脚本: {}", scriptFile.getAbsolutePath());

        // 创建GroovyShell
        GroovyShell shell = new GroovyShell();

        // 解析脚本
        Script script = shell.parse(scriptFile);

        // 执行脚本并获取结果
        Object result = script.run();

        if (!(result instanceof FlowDefinition)) {
            logger.warn("Channel复杂流程DSL脚本执行结果不是FlowDefinition类型: {}, 使用模拟流程定义",
                result != null ? result.getClass().getSimpleName() : "null");
            return createMockChannelFlowDefinition();
        }

        FlowDefinition flowDefinition = (FlowDefinition)result;
        logger.info("Channel复杂流程DSL脚本解析成功，流程名称: {}", flowDefinition.getName());

        return flowDefinition;
    }

    /**
     * 创建模拟的Channel流程定义用于测试
     */
    private FlowDefinition createMockChannelFlowDefinition() {
        // 创建一个简单的模拟Channel流程定义
        FlowDefinition mockFlow = new FlowDefinition();
        mockFlow.setName("模拟Channel通信流程");
        mockFlow.setDescription("用于测试的模拟Channel流程");
        return mockFlow;
    }

    /**
     * 运行多个并发Channel流程示例
     */
    public void runConcurrentChannelExample() {
        try {
            logger.info("=== 开始并发Channel复杂流程示例 ===");

            // 解析DSL脚本
            FlowDefinition flowDefinition = parseDslScript();

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(8);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 创建多个并发任务
            CompletableFuture<FlowResult>[] futures = new CompletableFuture[3];

            for (int i = 0; i < 3; i++) {
                int orderIndex = i;

                Map<String, Object> initialParams = new HashMap<>();
                initialParams.put("orderId", "CHANNEL_CONCURRENT_" + orderIndex + "_" + System.currentTimeMillis());
                initialParams.put("userEmail", "channel" + orderIndex + "@example.com");
                initialParams.put("userPhone", "1390013900" + orderIndex);

                futures[i] = engine.execute(flowDefinition, initialParams)
                    .thenApply(result -> {
                        logger.info("并发Channel任务 {} 完成，订单ID: {}", orderIndex,
                            initialParams.get("orderId"));
                        return result;
                    });
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures).join();

            logger.info("所有并发Channel任务执行完成！");

            // 输出结果
            for (int i = 0; i < futures.length; i++) {
                FlowResult result = futures[i].get();
                logger.info("Channel任务 {} 结果: 状态={}, 执行ID={}",
                    i, result.getStatus(), result.getExecutionId());
            }

            executor.shutdown();

        } catch (Exception e) {
            logger.error("并发Channel示例执行失败", e);
        }
    }

    /**
     * 运行Channel流程性能测试
     */
    public void runChannelPerformanceTest() {
        try {
            logger.info("=== 开始Channel流程性能测试 ===");

            // 解析DSL脚本
            FlowDefinition flowDefinition = parseDslScript();

            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(16);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);

            // 记录开始时间
            long startTime = System.currentTimeMillis();

            // 创建多个并发任务
            CompletableFuture<FlowResult>[] futures = new CompletableFuture[10];

            for (int i = 0; i < 10; i++) {
                int orderIndex = i;

                Map<String, Object> initialParams = new HashMap<>();
                initialParams.put("orderId", "PERF_CHANNEL_" + orderIndex + "_" + System.currentTimeMillis());
                initialParams.put("userEmail", "perf" + orderIndex + "@example.com");
                initialParams.put("userPhone", "1390013900" + orderIndex);

                futures[i] = engine.execute(flowDefinition, initialParams);
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures).join();

            // 记录结束时间
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            logger.info("Channel流程性能测试完成！");
            logger.info("总执行时间: {} ms", totalTime);
            logger.info("平均每个流程执行时间: {} ms", totalTime / 10.0);

            // 统计成功和失败数量
            int successCount = 0;
            int errorCount = 0;
            for (CompletableFuture<FlowResult> future : futures) {
                FlowResult result = future.get();
                if (result.isError()) {
                    errorCount++;
                } else {
                    successCount++;
                }
            }

            logger.info("成功执行: {} 个", successCount);
            logger.info("失败执行: {} 个", errorCount);
            logger.info("成功率: {}%", (successCount * 100.0) / futures.length);

            executor.shutdown();

        } catch (Exception e) {
            logger.error("Channel流程性能测试失败", e);
        }
    }
} 