package source.hanger.flow.contract.runtime.channel;

import java.util.Map;

/**
 * FlowDataChunk 定义channel接收的数据块结构
 * 支持不同类型的数据交换，step之间通过此结构进行异步通信
 */
public class FlowDataChunk {

    private final String bufferId;
    private final DataType dataType;
    private final TransferStatus transferStatus;
    private final Object data;
    private final long timestamp;
    private final String sourceStep;
    private final Map<String, Object> metadata;

    public FlowDataChunk(String bufferId, DataType dataType, TransferStatus transferStatus,
        Object data, String sourceStep, Map<String, Object> metadata) {
        this.bufferId = bufferId;
        this.dataType = dataType;
        this.transferStatus = transferStatus;
        this.data = data;
        this.sourceStep = sourceStep;
        this.metadata = metadata;
        timestamp = System.currentTimeMillis();
    }

    // Getter方法
    public String getBufferId() {
        return bufferId;
    }

    public DataType getDataType() {
        return dataType;
    }

    public TransferStatus getTransferStatus() {
        return transferStatus;
    }

    public Object getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSourceStep() {
        return sourceStep;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // 状态检查方法
    public boolean isFragment() {
        return transferStatus == TransferStatus.FRAGMENT;
    }

    public boolean isDone() {
        return transferStatus == TransferStatus.DONE;
    }

    public boolean isError() {
        return transferStatus == TransferStatus.ERROR;
    }

    public boolean isCancelled() {
        return transferStatus == TransferStatus.CANCELLED;
    }

    public boolean isTimeout() {
        return transferStatus == TransferStatus.TIMEOUT;
    }

    public boolean isCompleted() {
        return transferStatus == TransferStatus.DONE || 
               transferStatus == TransferStatus.ERROR ||
               transferStatus == TransferStatus.CANCELLED ||
               transferStatus == TransferStatus.TIMEOUT;
    }

    // 数据类型检查方法
    public boolean isJson() {
        return dataType == DataType.JSON;
    }

    public boolean isString() {
        return dataType == DataType.STRING;
    }

    public boolean isBinary() {
        return dataType == DataType.BINARY;
    }

    public boolean isRaw() {
        return dataType == DataType.RAW;
    }

    // 类型安全的数据获取方法
    public Object getJsonData() {
        if (isJson()) {
            return data;
        }
        throw new IllegalStateException("Data chunk is not JSON type");
    }

    public String getStringData() {
        if (isString()) {
            return (String) data;
        }
        throw new IllegalStateException("Data chunk is not STRING type");
    }

    public byte[] getBinaryData() {
        if (isBinary()) {
            return (byte[]) data;
        }
        throw new IllegalStateException("Data chunk is not BINARY type");
    }

    public Throwable getErrorData() {
        if (isError()) {
            return (Throwable) data;
        }
        throw new IllegalStateException("Data chunk is not ERROR status");
    }

    @Override
    public String toString() {
        return "FlowDataChunk{" +
            "bufferId='" + bufferId + '\'' +
            ", dataType=" + dataType +
            ", transferStatus=" + transferStatus +
            ", data=" + data +
            ", timestamp=" + timestamp +
            ", sourceStep='" + sourceStep + '\'' +
            ", metadata=" + metadata +
            '}';
    }
}