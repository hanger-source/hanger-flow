package source.hanger.flow.contract.runtime.common;

/**
 * 流程统一闭包接口
 * 定义了流程执行过程中的各种处理逻辑，本质上是 Groovy Closure 的 Java 封装
 * 包括任务执行、进入处理、错误处理等场景
 * 
 * @author fuhangbo.hanger.uhfun
 **/
@FunctionalInterface
public interface FlowClosure {
    /**
     * 执行闭包逻辑
     * 根据使用场景不同，可能代表：
     * - 任务主体执行 (run)
     * - 进入处理逻辑 (handle)
     * - 错误处理逻辑 (handle)
     *
     * @param access 流程执行访问接口，提供执行过程中的上下文访问能力
     */
    void call(FlowRuntimeExecuteAccess access);
} 