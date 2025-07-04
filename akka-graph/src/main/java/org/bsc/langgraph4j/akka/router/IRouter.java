package org.bsc.langgraph4j.akka.router;

import org.bsc.langgraph4j.akka.model.State;

/**
 * IRouter 定义节点间路由策略接口。
 * 典型实现如 LinearRouter、ConditionalRouter。
 * 用于根据当前 State 和节点输出决定下一个节点。
 */
public interface IRouter {
    /**
     * 根据当前状态和输出决定下一个节点ID。
     * @param state 当前全局状态
     * @param output 节点输出
     * @return 下一个节点ID
     */
    String route(State state, Object output);
} 