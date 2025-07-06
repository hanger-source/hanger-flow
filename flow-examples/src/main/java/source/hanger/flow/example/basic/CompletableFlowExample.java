package source.hanger.flow.example.basic;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.execution.FlowResult;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture流程引擎示例
 *
 * 演示如何使用CompletableFlowEngine执行从DSL解析的流程定义
 *
 * @author fuhangbo.hanger.uhfun
 */
public class CompletableFlowExample {

    private static final Logger logger = LoggerFactory.getLogger(CompletableFlowExample.class);

    public static void main(String[] args) {
        CompletableFlowExample example = new CompletableFlowExample();
        example.runExample();
    }

    /**
     * 运行示例
     */
    public void runExample() {
        try {
            logger.info("=== 开始CompletableFuture流程引擎示例 ===");

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
            initialParams.put("orderId", "ORDER_" + System.currentTimeMillis());
            initialParams.put("userEmail", "user@example.com");
            initialParams.put("userPhone", "13800138000");

            logger.info("开始执行流程，订单ID: {}", initialParams.get("orderId"));

            // 4. 异步执行流程
            CompletableFuture<FlowResult> future = engine.execute(flowDefinition, initialParams);

            // 5. 等待执行完成并获取结果
            FlowResult result = future.get();

            // 6. 输出执行结果
            logger.info("流程执行完成！");
            logger.info("执行状态: {}", result.getStatus());
            logger.info("执行ID: {}", result.getExecutionId());
            logger.info("最终输入: {}", result.getAttributes());
            if (result.isError()) {
                logger.error("执行失败，错误: {}", result.getError());
            }

            // 7. 关闭线程池
            executor.shutdown();

        } catch (Exception e) {
            logger.error("示例执行失败", e);
        }
    }

    /**
     * 解析DSL脚本
     *
     * @return 流程定义
     * @throws Exception 解析异常
     */
    private FlowDefinition parseDslScript() throws Exception {
        // 获取脚本文件路径
        // 在hanger-flow下 运行：mvn exec:java -Dexec.mainClass="source.hanger.flow.example.basic.CompletableFlowExample"
        String scriptPath = "flow-examples/src/main/resources/script/MyComplexProcess.groovy";
        File scriptFile = new File(scriptPath);

        if (!scriptFile.exists()) {
            throw new RuntimeException("找不到DSL脚本文件: " + scriptPath);
        }

        logger.info("开始解析DSL脚本: {}", scriptFile.getAbsolutePath());

        // 创建GroovyShell
        GroovyShell shell = new GroovyShell();

        // 解析脚本
        Script script = shell.parse(scriptFile);

        // 执行脚本并获取结果
        Object result = script.run();

        if (!(result instanceof FlowDefinition)) {
            logger.warn("DSL脚本执行结果不是FlowDefinition类型: {}, 使用模拟流程定义",
                result != null ? result.getClass().getSimpleName() : "null");
            return createMockFlowDefinition();
        }

        FlowDefinition flowDefinition = (FlowDefinition)result;
        logger.info("DSL脚本解析成功，流程名称: {}", flowDefinition.getName());

        return flowDefinition;
    }

    /**
     * 创建模拟的流程定义用于测试
     */
    private FlowDefinition createMockFlowDefinition() {
        // 创建一个简单的模拟流程定义
        FlowDefinition mockFlow = new FlowDefinition();
        mockFlow.setName("模拟订单处理流程");
        mockFlow.setDescription("用于测试的模拟流程");
        return mockFlow;
    }

    /**
     * 运行多个并发示例
     */
    public void runConcurrentExample() {
        try {
            logger.info("=== 开始并发CompletableFuture流程引擎示例 ===");

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
                initialParams.put("orderId", "ORDER_CONCURRENT_" + orderIndex + "_" + System.currentTimeMillis());
                initialParams.put("userEmail", "user" + orderIndex + "@example.com");
                initialParams.put("userPhone", "1380013800" + orderIndex);

                futures[i] = engine.execute(flowDefinition, initialParams)
                    .thenApply(result -> {
                        logger.info("并发任务 {} 完成，订单ID: {}", orderIndex,
                            initialParams.get("orderId"));
                        return result;
                    });
            }

            // 等待所有任务完成
            CompletableFuture.allOf(futures).join();

            logger.info("所有并发任务执行完成！");

            // 输出结果
            for (int i = 0; i < futures.length; i++) {
                FlowResult result = futures[i].get();
                logger.info("任务 {} 结果: 状态={}, 执行ID={}",
                    i, result.getStatus(), result.getExecutionId());
            }

            executor.shutdown();

        } catch (Exception e) {
            logger.error("并发示例执行失败", e);
        }
    }
} 