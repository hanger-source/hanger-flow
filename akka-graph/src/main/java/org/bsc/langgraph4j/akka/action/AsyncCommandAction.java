package org.bsc.langgraph4j.akka.action;

import org.bsc.langgraph4j.akka.model.Command;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * 异步命令节点动作接口。
 * @param <S> 状态类型
 */
public interface AsyncCommandAction<S> extends BiFunction<S, Object, CompletableFuture<Command>> {
} 