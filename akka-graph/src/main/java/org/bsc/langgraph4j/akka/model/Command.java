package org.bsc.langgraph4j.akka.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Command 封装节点执行后的路由和状态更新信息。
 * 用于节点动作返回下一个节点ID和状态。
 */
public class Command implements Serializable {
    private final String gotoNode;
    private final Map<String, Object> update;

    /**
     * 构造函数。
     * @param gotoNode 下一个节点ID
     * @param update 状态更新Map
     */
    public Command(String gotoNode, Map<String, Object> update) {
        this.gotoNode = gotoNode;
        this.update = update;
    }

    /**
     * 获取下一个节点ID。
     */
    public String gotoNode() {
        return gotoNode;
    }

    /**
     * 获取状态更新Map。
     */
    public Map<String, Object> update() {
        return update;
    }
} 