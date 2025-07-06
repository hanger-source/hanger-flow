package source.hanger.flow.contract.runtime.channel;

/**
 * FlowStreamingChannel 定义流程引擎中的流式channel通信能力
 * 使用者只关心接收数据和推送数据，不需要关心内部实现细节
 * step之间通过FlowDataChunk进行异步数据交换
 */
public interface FlowStreamingChannel {
    /**
     * 推送数据
     * channel.acquireBuffer('').pushFragment('')
     * 获取缓冲区（如果不存在则创建）
     */
    FlowDataChunkBuffer acquireBuffer(String bufferId);

    /**
     * 接受数据
     * channel.getBuffer('').onReceive(new FlowDataChunkListener() {
     * })
     * 获取已存在的缓冲区
     */
    FlowDataChunkBuffer getBuffer(String bufferId);

    /**
     * 释放缓冲区
     */
    void releaseBuffer(String bufferId);

    /**
     * 检查缓冲区是否存在
     */
    boolean hasBuffer(String bufferId);
}