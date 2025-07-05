package source.hanger.flow.contract.runtime.common;

/**
 * 流程运行时访问接口
 * 定义了流程执行过程中对上下文和日志的访问能力
 * 
 * @param <Context> 上下文类型，必须继承自FlowRuntimeAccessContext
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowRuntimeAccess<Context> {
    /**
     * 获取流程的上下文数据 (可读写)
     * 
     * @return 流程上下文数据
     */
    Context getContext();

    /**
     * 记录任务日志
     *
     * @param message 日志内容
     */
    void log(String message);
}
