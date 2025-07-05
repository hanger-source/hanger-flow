package hanger.source.akka.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * 异步节点动作接口。
 * @param <S> 状态类型
 */
@FunctionalInterface
public interface AsyncNodeAction<S> extends Function<S, CompletableFuture<Map<String, Object>>> {
    /**
     * 执行异步节点动作。
     * @param state 当前状态
     * @return 异步状态更新Map
     */
    CompletableFuture<Map<String, Object>> apply(S state);
} 