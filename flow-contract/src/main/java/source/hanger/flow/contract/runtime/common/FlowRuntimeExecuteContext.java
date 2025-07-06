package source.hanger.flow.contract.runtime.common;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;
import source.hanger.flow.contract.runtime.wrapper.FlowStreamingChannelSafeWrapper;

/**
 * 流程运行时访问上下文抽象基类
 * 提供了流程执行过程中的上下文数据存储能力
 * 支持序列化，可用于分布式场景
 *
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class FlowRuntimeExecuteContext extends LinkedHashMap<String, Object> implements Serializable {
    @Serial
    private static final long serialVersionUID = 6473185372366539085L;
    private String executionId;
    private String version;
    private String flowName;
    private String stepName;
    private Throwable exception;
    private FlowStreamingChannel channel;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取输入参数映射（DSL中修改会静默忽略）
     *
     * @return 输入参数映射
     */
    public abstract Map<String, Object> getInputs();

    /**
     * 获取指定的输入参数
     *
     * @param key 参数键
     * @return 参数值，如果不存在则返回null
     */
    public abstract Object getInput(String key);

    /**
     * 获取指定的输入参数，如果不存在则返回默认值
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public abstract Object getInput(String key, Object defaultValue);

    /**
     * 获取当前step的channel
     *
     * @return FlowStreamingChannel实例
     */
    public FlowStreamingChannel getChannel() {
        return channel;
    }

    /**
     * 设置当前step的channel
     *
     * @param channel FlowStreamingChannel实例
     */
    public void setChannel(FlowStreamingChannel channel) {
        this.channel = new FlowStreamingChannelSafeWrapper(channel);
    }
}
