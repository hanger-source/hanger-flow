package source.hanger.flow.completable.runtime;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.hanger.flow.completable.runtime.step.TaskStepExecutor;
import source.hanger.flow.contract.model.*;
import source.hanger.flow.contract.runtime.channel.FlowDataChunk;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkBuffer;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkListener;
import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;
import source.hanger.flow.contract.runtime.common.FlowClosure;
import source.hanger.flow.core.runtime.channel.DefaultFlowChannel;
import source.hanger.flow.core.runtime.execution.FlowExecutionContext;
import source.hanger.flow.core.runtime.execution.FlowResult;
import source.hanger.flow.core.runtime.status.FlowStatus;
import source.hanger.flow.core.runtime.step.StepExecutionCallback;
import source.hanger.flow.core.runtime.step.StepExecutionHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * CompletableFlowEngine单元测试（新版channel buffer兼容）
 */
public class CompletableFlowEngineTest {

    private static final Logger log = LoggerFactory.getLogger(CompletableFlowEngineTest.class);

    @Test
    public void testStreamingTaskExecution() throws Exception {
        // 创建支持流式的任务步骤
        TaskStepDefinition streamingTask = new TaskStepDefinition();
        streamingTask.setName("__START__");
        streamingTask.setStreamingSupported(true);
        streamingTask.setOutputType(String.class);
        streamingTask.setTaskRunnable(access -> {
            // 通过channel推送流式数据
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-1");
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-2");
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-3");
            access.getChannel().acquireBuffer("streaming").pushDone();
        });

        // 创建流程定义
        FlowDefinition flowDef = new FlowDefinition();
        flowDef.setName("streamingTest");
        flowDef.setVersion("1.0");
        flowDef.addStep(streamingTask);

        // 创建引擎
        CompletableFlowEngine engine = new CompletableFlowEngine();

        // 执行流程
        CompletableFuture<FlowResult> future = engine.execute(flowDef);

        // 等待完成
        FlowResult result = future.get(5, TimeUnit.SECONDS);

        // 验证结果
        assertNotNull(result);
        assertEquals(FlowStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getExecutionId());
    }

    @Test
    public void testStreamingFragmentCallback() throws Exception {
        // 创建支持流式的任务步骤
        TaskStepDefinition streamingTask = new TaskStepDefinition();
        streamingTask.setName("__START__");
        streamingTask.setStreamingSupported(true);
        streamingTask.setOutputType(String.class);
        // fragments 和 finalResult 提前声明
        List<Object> fragments = new ArrayList<>();
        AtomicReference<Object> finalResult = new AtomicReference<>();
        // 在 taskRunnable 内注册监听并推送数据
        streamingTask.setTaskRunnable(access -> {
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-1");
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-2");
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-3");
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-4");
            access.getChannel().acquireBuffer("streaming").pushFragment("fragment-5");
            access.getChannel().acquireBuffer("streaming").pushDone();

            access.getChannel().getBuffer("streaming").onReceive(chunk -> {
                if (chunk.isFragment()) {
                    access.log("onReceive streaming " + chunk.getData());
                    fragments.add(chunk.getData());
                }
            });
        });
        // 创建流程定义
        FlowDefinition flowDef = new FlowDefinition();
        flowDef.setName("streamingTest");
        flowDef.setVersion("1.0");
        flowDef.addStep(streamingTask);
        // 创建引擎
        CompletableFlowEngine engine = new CompletableFlowEngine();
        CompletableFuture<FlowResult> future = engine.execute(flowDef);
        FlowResult result = future.get(5, TimeUnit.SECONDS);
        assertNotNull(result);
        assertEquals(FlowStatus.SUCCESS, result.getStatus());

        // 验证结果
        assertNotNull(result.getAttributes());
        assertTrue(result.getAttributes().containsKey("result"));
        // 等待数据收集
        TimeUnit.MILLISECONDS.sleep(500);
        // 验证fragment和最终结果
        assertTrue(fragments.size() >= 1);
    }

    /**
     * 创建任务处理器
     */
    private FlowClosure createTaskHandler(String taskName, long sleepTime) {
        return access -> {
            access.log("执行任务: " + taskName);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            access.log("任务完成: " + taskName);
        };
    }
} 