package source.hanger.akkagraph.action;

import source.hanger.akkagraph.model.Command;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * 异步命令节点动作接口。
 * @param <S> 状态类型
 */
public interface AsyncCommandAction<S> extends BiFunction<S, Object, CompletableFuture<Command>> {
} 