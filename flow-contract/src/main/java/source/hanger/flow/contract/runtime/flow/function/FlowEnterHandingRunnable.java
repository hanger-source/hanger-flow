package source.hanger.flow.contract.runtime.flow.function;

import source.hanger.flow.contract.runtime.flow.access.FlowEnterHandlingAccess;

/**
 * 流程进入处理接口
 * 定义了流程开始执行时的预处理逻辑，在流程正式执行前调用
 */
@FunctionalInterface
public interface FlowEnterHandingRunnable {
    /**
     * 处理流程进入逻辑
     *
     * @param access 流程进入处理访问接口，提供流程进入时的上下文访问能力
     */
    void handle(FlowEnterHandlingAccess access);
}