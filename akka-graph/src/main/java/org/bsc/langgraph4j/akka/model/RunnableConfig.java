package org.bsc.langgraph4j.akka.model;

import java.io.Serializable;
import java.util.Map;

/**
 * RunnableConfig 封装图运行时的配置参数。
 * 典型用法：传递给 GraphManagerActor 或 SubgraphActor，控制执行参数、超时、重试等。
 */
public class RunnableConfig implements Serializable {
    private final Map<String, Object> config;

    /**
     * 构造函数。
     * @param config 配置参数Map
     */
    public RunnableConfig(Map<String, Object> config) {
        this.config = config;
    }
    /**
     * 获取配置参数。
     */
    public Map<String, Object> config() {
        return config;
    }
} 