package hanger.source.akka.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * RunnableConfig 封装图运行时的配置参数。
 * 典型用法：传递给 GraphManagerActor 或 SubgraphActor，控制执行参数、超时、重试等。
 */
public record RunnableConfig(Map<String, Object> config) implements Serializable {
    @Serial
    private static final long serialVersionUID = -4900947835365971863L;

    /**
     * 构造函数。
     *
     * @param config 配置参数Map
     */
    public RunnableConfig {
    }

    /**
     * 获取配置参数。
     */
    @Override
    public Map<String, Object> config() {
        return config;
    }
} 