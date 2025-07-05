package source.hanger.flow.example.complex;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.hanger.flow.completable.runtime.CompletableFlowEngine;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.core.runtime.FlowResult;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuperComplexFlowExample {
    private static final Logger logger = LoggerFactory.getLogger(SuperComplexFlowExample.class);

    public static void main(String[] args) {
        SuperComplexFlowExample example = new SuperComplexFlowExample();
        example.runExample();
    }

    public void runExample() {
        try {
            logger.info("=== 开始超级复杂流程引擎示例 ===");
            FlowDefinition flowDefinition = parseDslScript();
            logger.info("DSL脚本解析完成，流程名称: {}", flowDefinition.getName());
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
            ExecutorService executor = Executors.newFixedThreadPool(8);
            CompletableFlowEngine engine = new CompletableFlowEngine(executor);
            Map<String, Serializable> initialParams = new HashMap<>();
            initialParams.put("orderId", "ORDER_SUPER_" + System.currentTimeMillis());
            initialParams.put("userEmail", "superuser@example.com");
            initialParams.put("userPhone", "13900000000");
            logger.info("开始执行流程，订单ID: {}", initialParams.get("orderId"));
            CompletableFuture<FlowResult> future = engine.execute(flowDefinition, initialParams);
            FlowResult result = future.get();
            logger.info("流程执行完成！");
            logger.info("执行状态: {}", result.getStatus());
            logger.info("执行ID: {}", result.getExecutionId());
            logger.info("最终参数: {}", result.getParams());
            if (result.isError()) {
                logger.error("执行失败，错误: {}", result.getError());
            }
            executor.shutdown();
        } catch (Exception e) {
            logger.error("超级复杂流程示例执行失败", e);
        }
    }

    private FlowDefinition parseDslScript() throws Exception {
        String scriptPath = "flow-examples/src/main/resources/script/SuperComplexProcess.groovy";
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            throw new RuntimeException("找不到DSL脚本文件: " + scriptPath);
        }
        logger.info("开始解析DSL脚本: {}", scriptFile.getAbsolutePath());
        GroovyShell shell = new GroovyShell();
        Script script = shell.parse(scriptFile);
        Object result = script.run();
        if (!(result instanceof FlowDefinition)) {
            logger.warn("DSL脚本执行结果不是FlowDefinition类型: {}, 使用模拟流程定义", result != null ? result.getClass().getSimpleName() : "null");
            return createMockFlowDefinition();
        }
        FlowDefinition flowDefinition = (FlowDefinition)result;
        logger.info("DSL脚本解析成功，流程名称: {}", flowDefinition.getName());
        return flowDefinition;
    }

    private FlowDefinition createMockFlowDefinition() {
        FlowDefinition mockFlow = new FlowDefinition();
        mockFlow.setName("模拟超级复杂流程");
        mockFlow.setDescription("用于测试的模拟超级复杂流程");
        return mockFlow;
    }
} 