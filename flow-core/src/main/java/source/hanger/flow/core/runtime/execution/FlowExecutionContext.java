package source.hanger.flow.core.runtime.execution;

import org.apache.commons.lang3.SerializationUtils;
import source.hanger.flow.contract.model.FlowDefinition;
import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;
import source.hanger.flow.core.support.SilentUnmodifiableMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程执行上下文
 * <p>
 * 作用：
 * - 封装流程执行过程中的上下文信息
 * - 提供参数存储、流程定义访问等功能
 * - 线程安全，支持并发访问
 * - inputs是不可变的，确保数据一致性
 * - DSL中的修改操作会静默忽略，不抛异常
 */
public class FlowExecutionContext {
    /**
     * 执行ID
     */
    private final String executionId;
    /**
     * 流程定义
     */
    private final FlowDefinition flowDefinition;
    /**
     * 执行输入（不可变，但DSL中修改会静默忽略）
     */
    private final Map<String, Object> inputs;
    /**
     *
     */
    private final Map<String, Object> attributes;
    /**
     * 流程channel
     */
    private FlowStreamingChannel channel;

    public FlowExecutionContext(String executionId, FlowDefinition flowDefinition,
        Map<String, Object> initialInputs) {
        this.executionId = executionId;
        this.flowDefinition = flowDefinition;
        // 创建不可变的inputs，但使用装饰器让修改操作静默忽略
        inputs = new SilentUnmodifiableMap<>(initialInputs);
        attributes = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public FlowExecutionContext(FlowExecutionContext context) {
        this(context.executionId, context.flowDefinition, context.inputs);
        Map<String, Object> clonedAttributes = (Map<String, Object>)SerializationUtils
            .clone((Serializable)context.getAttributes());
        attributes.putAll(clonedAttributes);
        setChannel(context.getChannel());
    }

    public String getExecutionId() {
        return executionId;
    }

    public FlowDefinition getFlowDefinition() {
        return flowDefinition;
    }

    /**
     * 获取输入参数映射（DSL中修改会静默忽略）
     *
     * @return 输入参数映射
     */
    public Map<String, Object> getInputs() {
        return inputs;
    }

    /**
     * 获取指定的输入参数
     *
     * @param key 参数键
     * @return 参数值，如果不存在则返回null
     */
    public Object getInput(String key) {
        return inputs.get(key);
    }

    /**
     * 获取指定的输入参数，如果不存在则返回默认值
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public Object getInput(String key, Object defaultValue) {
        return inputs.getOrDefault(key, defaultValue);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCloneAttributes() {
        Map<String, Object> clonedAttributes = (Map<String, Object>)SerializationUtils
            .clone((Serializable)getAttributes());
        return new HashMap<>(clonedAttributes);
    }

    /**
     * 获取流程channel
     *
     * @return FlowStreamingChannel实例
     */
    public FlowStreamingChannel getChannel() {
        return channel;
    }

    /**
     * 设置流程channel
     *
     * @param channel FlowStreamingChannel实例
     */
    public void setChannel(FlowStreamingChannel channel) {
        this.channel = channel;
    }
}
