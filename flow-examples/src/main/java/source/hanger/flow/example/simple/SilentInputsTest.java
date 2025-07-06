package source.hanger.flow.example.simple;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.completable.runtime.context.CompletableFlowRuntimeExecuteContext;
import source.hanger.flow.completable.runtime.access.CompletableFlowRuntimeExecuteAccess;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 测试inputs的静默忽略功能
 */
public class SilentInputsTest {

    private static final Logger logger = LoggerFactory.getLogger(SilentInputsTest.class);

    public static void main(String[] args) {
        SilentInputsTest test = new SilentInputsTest();
        test.testSilentInputs();
    }

    public void testSilentInputs() {
        try {
            logger.info("=== 开始测试inputs静默忽略功能 ===");

            // 1. 创建流程定义
            FlowDefinition flowDefinition = new FlowDefinition();
            flowDefinition.setName("测试流程");

            // 2. 创建初始参数
            Map<String, Object> initialInputs = new HashMap<>();
            initialInputs.put("orderId", "TEST_ORDER_123");
            initialInputs.put("userName", "测试用户");
            initialInputs.put("amount", 100.0);

            // 3. 创建FlowExecutionContext
            FlowExecutionContext flowContext = new FlowExecutionContext(
                "test-execution-id",
                flowDefinition,
                initialInputs
            );

            // 4. 创建CompletableFlowRuntimeExecuteContext
            CompletableFlowRuntimeExecuteContext context = new CompletableFlowRuntimeExecuteContext(
                flowContext,
                "test-step"
            );

            // 5. 创建CompletableFlowRuntimeExecuteAccess
            CompletableFlowRuntimeExecuteAccess access = new CompletableFlowRuntimeExecuteAccess(context);

            // 6. 测试inputs的静默忽略功能
            testSilentInputs(access);

            logger.info("✅ inputs静默忽略测试成功！");

        } catch (Exception e) {
            logger.error("❌ inputs静默忽略测试失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void testSilentInputs(FlowRuntimeExecuteAccess access) {
        logger.info("开始测试inputs静默忽略功能...");

        // 获取inputs
        Map<String, Object> inputs = access.getInputs();
        logger.info("获取到的inputs: {}", inputs);

        // 尝试修改inputs（应该静默忽略，不抛异常）
        try {
            logger.info("尝试修改inputs...");

            // 测试put操作
            Object oldValue = inputs.put("newKey", "newValue");
            logger.info("put操作结果: oldValue={}", oldValue);

            // 测试remove操作
            Object removedValue = inputs.remove("orderId");
            logger.info("remove操作结果: removedValue={}", removedValue);

            // 测试clear操作
            inputs.clear();
            logger.info("clear操作完成");

            // 检查inputs是否真的被修改了
            logger.info("修改后的inputs: {}", inputs);
            logger.info("inputs大小: {}", inputs.size());

            // 验证inputs实际上没有被修改（因为静默忽略）
            if (inputs.containsKey("orderId")) {
                logger.info("✅ 正确：inputs没有被修改，orderId仍然存在");
            } else {
                logger.error("❌ 错误：inputs被修改了，orderId不存在");
            }

        } catch (Exception e) {
            logger.error("❌ 错误：应该静默忽略，但抛出了异常: {}", e.getMessage());
            e.printStackTrace();
        }

        // 测试获取单个input
        Object orderId = access.getInput("orderId");
        logger.info("获取orderId: {}", orderId);

        Object userName = access.getInput("userName");
        logger.info("获取userName: {}", userName);

        Object amount = access.getInput("amount");
        logger.info("获取amount: {}", amount);

        // 测试获取不存在的key
        Object nonExistent = access.getInput("nonExistent");
        logger.info("获取不存在的key: {}", nonExistent);

        // 测试获取不存在的key，带默认值
        Object withDefault = access.getInput("nonExistent", "默认值");
        logger.info("获取不存在的key（带默认值）: {}", withDefault);

        logger.info("inputs静默忽略测试完成");
    }
} 