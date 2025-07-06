package source.hanger.flow.contract.runtime.channel;

/**
 * FlowDataChunkListener
 * 定义缓冲区数据监听器接口
 * 用于接收缓冲区的数据变化通知
 */
public interface FlowDataChunkListener {

    /**
     * 接收到数据
     */
    void onReceive(FlowDataChunk chunk);
}