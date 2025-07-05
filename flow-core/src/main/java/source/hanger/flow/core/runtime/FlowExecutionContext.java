package source.hanger.flow.core.runtime;

import source.hanger.flow.contract.model.FlowDefinition;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程执行上下文
 * <p>
 * 作用：
 * - 封装流程执行过程中的上下文信息
 * - 提供参数存储、流程定义访问等功能
 * - 线程安全，支持并发访问
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
     * 执行参数
     */
    private final Map<String, Serializable> params;

    public FlowExecutionContext(String executionId, FlowDefinition flowDefinition,
        Map<String, ? extends Serializable> initialParams) {
        this.executionId = executionId;
        this.flowDefinition = flowDefinition;
        params = new ConcurrentHashMap<>();
        if (initialParams != null) {
            for (Map.Entry<String, ? extends Serializable> entry : initialParams.entrySet()) {
                params.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public String getExecutionId() {
        return executionId;
    }

    public FlowDefinition getFlowDefinition() {
        return flowDefinition;
    }

    public Map<String, Serializable> getParams() {
        return params;
    }

    public void setParam(String key, Serializable value) {
        params.put(key, value);
    }

    public Serializable getParam(String key) {
        return params.get(key);
    }

    public Serializable getParam(String key, Serializable defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }

    public Serializable removeParam(String key) {
        return params.remove(key);
    }

    public void clearParams() {
        params.clear();
    }
}
