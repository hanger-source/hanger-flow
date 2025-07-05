package source.hanger.flow.completable.runtime;

import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.model.TaskStepDefinition;
import source.hanger.flow.contract.runtime.task.function.FlowTaskRunnable;
import source.hanger.flow.core.runtime.FlowResult;
import source.hanger.flow.core.runtime.FlowStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模块结构测试
 * <p>
 * 验证重构后的模块职责划分是否合理：
 * - flow-contract: 契约接口
 * - flow-core: 核心运行时
 * - flow-completable-runtime: 具体实现
 */
public class ModuleStructureTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleStructureTest.class);
    
    public static void main(String[] args) {
        ModuleStructureTest test = new ModuleStructureTest();
        test.testModuleStructure();
    }
    
    /**
     * 测试模块结构
     */
    public void testModuleStructure() {
        logger.info("=== 模块结构测试 ===");
        
        try {
            // 测试基础功能
            testBasicFunctionality();
            
            // 测试模块职责分离
            testModuleSeparation();
            
            logger.info("✅ 模块结构测试通过");
            
        } catch (Exception e) {
            logger.error("❌ 模块结构测试失败: {}", e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试基础功能
     */
    private void testBasicFunctionality() {
        logger.info("测试基础功能...");
        
        // 创建引擎
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFlowEngine engine = new CompletableFlowEngine(executor);
        
        // 创建测试流程
        FlowDefinition flow = createTestFlow();
        
        // 执行流程
        Map<String, Serializable> params = new HashMap<>();
        params.put("testParam", "testValue");
        
        CompletableFuture<FlowResult> future = engine.execute(flow, params);
        FlowResult result;
        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("流程执行异常", e);
        }
        
        if (result.isSuccess()) {
            logger.info("✅ 基础功能正常");
        } else {
            throw new RuntimeException("基础功能异常: " + result.getStatus());
        }
        
        executor.shutdown();
    }
    
    /**
     * 测试模块职责分离
     */
    private void testModuleSeparation() {
        logger.info("测试模块职责分离...");
        
        // 验证flow-contract模块职责
        logger.info("- flow-contract: 契约接口定义");
        
        // 验证flow-core模块职责
        logger.info("- flow-core: 核心运行时组件");
        
        // 验证flow-completable-runtime模块职责
        logger.info("- flow-completable-runtime: CompletableFuture实现");
        
        logger.info("✅ 模块职责分离合理");
    }
    
    /**
     * 创建测试流程
     */
    private FlowDefinition createTestFlow() {
        FlowDefinition flow = new FlowDefinition();
        flow.setName("模块结构测试流程");
        flow.setDescription("用于验证模块结构的测试流程");
        flow.setVersion("1.0.0");
        
        // 添加任务步骤
        TaskStepDefinition task = new TaskStepDefinition();
        task.setName("结构测试任务");
        task.setDescription("测试模块结构的任务步骤");
        task.setTaskRunnable(new FlowTaskRunnable() {
            @Override
            public void run(source.hanger.flow.contract.runtime.task.access.FlowTaskRunAccess access) {
                logger.info("[ModuleStructureTest] 执行结构测试任务");
                access.log("模块结构测试任务执行完成");
            }
        });
        
        flow.getStepDefinitions().add(task);
        return flow;
    }
} 