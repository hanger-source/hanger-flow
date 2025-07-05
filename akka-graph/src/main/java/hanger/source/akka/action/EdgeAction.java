package hanger.source.akka.action;

/**
 * 同步边动作接口。
 * @param <S> 状态类型
 */
@FunctionalInterface
public interface EdgeAction<S> {
    /**
     * 执行边动作，返回下一个节点ID。
     * @param state 当前状态
     * @return 下一个节点ID
     * @throws Exception 业务异常
     */
    String apply(S state) throws Exception;
} 