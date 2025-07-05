package source.hanger.flow.contract.runtime.flow.function;

import source.hanger.flow.contract.runtime.flow.access.FlowErrorHandlingAccess;

/**
 * 流程错误处理接口
 * 定义了流程执行过程中发生错误时的处理逻辑
 */
@FunctionalInterface
public interface FlowErrorHandingRunnable {
    /**
     * 处理流程错误
     * 
     * @param access 流程错误处理访问接口，提供错误处理时的上下文访问能力
     */
    void handle(FlowErrorHandlingAccess access);
}