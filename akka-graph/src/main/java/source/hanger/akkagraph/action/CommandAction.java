package source.hanger.akkagraph.action;

import source.hanger.akkagraph.model.Command;

/**
 * 同步命令节点动作接口。
 * @param <S> 状态类型
 */
@FunctionalInterface
public interface CommandAction<S> {
    /**
     * 执行命令节点动作。
     * @param state 当前状态
     * @param config 运行时配置
     * @return 命令对象
     * @throws Exception 业务异常
     */
    Command apply(S state, Object config) throws Exception;
} 