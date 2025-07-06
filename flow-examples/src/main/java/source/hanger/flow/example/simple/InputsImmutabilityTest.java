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
 * 测试inputs的不可变性
 */
public class InputsImmutabilityTest {

    private static final Logger logger = LoggerFactory.getLogger(InputsImmutabilityTest.class);

    public static void main(String[] args) {
        InputsImmutabilityTest test = new InputsImmutabilityTest();
        test.testInputsImmutability();
    }

    public void testInputsImmutability() {
        try {
            logger.info("=== 开始测试inputs不可变性 ===");

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

            // 6. 测试inputs的不可变性
            testInputsImmutability(access);

            logger.info("✅ inputs不可变性测试成功！");

        } catch (Exception e) {
            logger.error("❌ inputs不可变性测试失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void testInputsImmutability(FlowRuntimeExecuteAccess access) {
        logger.info("开始测试inputs不可变性...");

        // 获取inputs
        Map<String, Object> inputs = access.getInputs();
        logger.info("获取到的inputs: {}", inputs);

        // 尝试修改inputs（应该抛出异常）
        try {
            inputs.put("newKey", "newValue");
            logger.error("❌ 错误：inputs应该是不可变的，但修改成功了");
        } catch (UnsupportedOperationException e) {
            logger.info("✅ 正确：inputs是不可变的，修改时抛出了UnsupportedOperationException");
        } catch (Exception e) {
            logger.info("✅ 正确：inputs是不可变的，修改时抛出了异常: {}", e.getClass().getSimpleName());
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

        logger.info("inputs不可变性测试完成");
    }
} 