package source.hanger.flow.contract.runtime.common;

import java.util.Map;

import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;

/**
 * 流程统一执行访问接口
 * 定义了流程执行过程中对上下文、日志和流式能力的访问
 *
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowRuntimeExecuteAccess {
    /**
     * 获取流程的上下文数据 (可读写)
     *
     * @return 流程上下文数据
     */
    FlowRuntimeExecuteContext getContext();

    /**
     * 获取输入参数映射（DSL中修改会静默忽略）
     *
     * @return 输入参数映射
     */
    default Map<String, Object> getInputs() {
        return getContext().getInputs();
    }

    /**
     * 获取指定的输入参数
     *
     * @param key 参数键
     * @return 参数值，如果不存在则返回null
     */
    default Object getInput(String key) {
        return getContext().getInput(key);
    }

    /**
     * 获取指定的输入参数，如果不存在则返回默认值
     *
     * @param key          参数键
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    default Object getInput(String key, Object defaultValue) {
        return getContext().getInput(key, defaultValue);
    }

    /**
     * 获取当前异常（onError场景下可用）
     *
     * @return 当前异常对象，如果无异常则为null
     */
    default Throwable getError() {
        return getContext().getException();
    }

    /**
     * 记录任务日志
     *
     * @param message 日志内容
     */
    void log(String message, Object... args);

    /**
     * 获取当前step的channel
     * 用于基于唯一标识的全局数据缓冲区操作
     *
     * @return FlowStreamingChannel实例
     */
    default FlowStreamingChannel getChannel() {
        return getContext().getChannel();
    }
}
