package source.hanger.flow.contract.runtime.channel;

import java.util.Map;

/**
 * FlowDataChunkBuffer 定义流程引擎中的缓冲区抽象类
 * 提供数据的存储、检索和监听能力
 */
public abstract class FlowDataChunkBuffer {

    private final Map<String, Object> metadata;
    protected String sourceStep;
    protected String sourceFlow;
    protected String bufferId;

    public FlowDataChunkBuffer() {
        metadata = Map.of();
    }

    public abstract void push(FlowDataChunk flowDataChunk);

    public void pushFragment(String data) {
        push(new FlowDataChunk(bufferId, DataType.STRING, TransferStatus.FRAGMENT, data, sourceStep, metadata));
    }

    public void pushFragment(byte[] data) {
        push(new FlowDataChunk(bufferId, DataType.BINARY, TransferStatus.FRAGMENT, data, sourceStep, metadata));
    }

    public void pushFragment(Object data) {
        push(new FlowDataChunk(bufferId, DataType.RAW, TransferStatus.FRAGMENT, data, sourceStep, metadata));
    }

    public void pushFragment(Object data, DataType dataType) {
        push(new FlowDataChunk(bufferId, dataType, TransferStatus.FRAGMENT, data, sourceStep, metadata));
    }

    public void pushDone() {
        push(new FlowDataChunk(bufferId, DataType.RAW, TransferStatus.DONE, null, sourceStep, metadata));
    }

    public void pushError(Throwable error) {
        push(new FlowDataChunk(bufferId, DataType.RAW, TransferStatus.ERROR, error, sourceStep, metadata));
    }

    public void pushCancelled() {
        push(new FlowDataChunk(bufferId, DataType.RAW, TransferStatus.CANCELLED, null, sourceStep, metadata));
    }

    /**
     * On receive.
     *
     * @param listener the listener
     */
    public abstract void onReceive(FlowDataChunkListener listener);

    /**
     * 关闭缓冲区
     */
    public void close() {
        // 默认实现为空，子类可以重写
    }

    // Getter方法
    public String getBufferId() {
        return bufferId;
    }

    public String getSourceStep() {
        return sourceStep;
    }

    public String getSourceFlow() {
        return sourceFlow;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}