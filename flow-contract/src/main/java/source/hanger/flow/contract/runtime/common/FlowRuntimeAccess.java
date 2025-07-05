package source.hanger.flow.contract.runtime.common;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowRuntimeAccess<Context> {
    /**
     * 流程的上下文数据 (可读写)
     */
    Context getContext();

    /**
     * 记录任务日志。
     *
     * @param message 日志内容。
     */
    void log(String message);
}
