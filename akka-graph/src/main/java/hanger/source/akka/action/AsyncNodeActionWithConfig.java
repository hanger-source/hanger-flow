package hanger.source.akka.action;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * 带配置的异步节点动作接口。
 * @param <S> 状态类型
 */
public interface AsyncNodeActionWithConfig<S> extends BiFunction<S, Object, CompletableFuture<Map<String, Object>>> {
    /**
     * 执行异步节点动作。
     * @param state 当前状态
     * @param config 运行时配置
     * @return 异步状态更新Map
     */
    CompletableFuture<Map<String, Object>> apply(S state, Object config);
} 