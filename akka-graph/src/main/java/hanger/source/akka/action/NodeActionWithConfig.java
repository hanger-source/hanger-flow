package hanger.source.akka.action;

/**
 * 带配置的同步节点动作接口。
 * @param <S> 状态类型
 *
 * 注意：本接口为低阶同步接口，推荐所有生产级节点实现均基于 INodeAction（流式FlowData）进行开发。
 */
public interface NodeActionWithConfig<S> {
    /**
     * 执行同步节点动作。
     * @param state 当前状态
     * @param config 运行时配置
     * @return 状态更新Map
     * @throws Exception 业务异常
     */
    java.util.Map<String, Object> apply(S state, Object config) throws Exception;
} 