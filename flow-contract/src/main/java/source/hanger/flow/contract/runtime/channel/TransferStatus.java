package source.hanger.flow.contract.runtime.channel;

/**
 * 传输状态枚举
 */
public enum TransferStatus {
    /**
     * 数据片段
     */
    FRAGMENT,
    /**
     * 传输完成
     */
    DONE,
    /**
     * 传输错误
     */
    ERROR,
    /**
     * 传输取消
     */
    CANCELLED,
    /**
     * 传输超时
     */
    TIMEOUT,

}
