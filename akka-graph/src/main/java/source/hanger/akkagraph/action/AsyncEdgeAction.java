package source.hanger.akkagraph.action;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 异步边动作接口。
 * @param <S> 状态类型
 */
@FunctionalInterface
public interface AsyncEdgeAction<S> extends Function<S, CompletableFuture<String>> {
    /**
     * 执行异步边动作，返回下一个节点ID。
     * @param state 当前状态
     * @return 异步下一个节点ID
     */
    CompletableFuture<String> apply(S state);
} 