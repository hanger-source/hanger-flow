package source.hanger.flow.core.runtime.buffer;

import source.hanger.flow.contract.runtime.channel.FlowDataChunkBuffer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FlowBufferManager 管理所有缓冲区的创建、查找和销毁
 * 提供缓冲区的生命周期管理
 */
public class FlowBufferManager {

    private final Map<String, FlowDataChunkBuffer> buffers;
    private final String managerId;

    public FlowBufferManager(String managerId) {
        this.managerId = managerId;
        buffers = new ConcurrentHashMap<>();
    }

    /**
     * 创建新的缓冲区
     *
     * @param bufferId 缓冲区唯一标识
     * @return 新创建的缓冲区
     */
    public FlowDataChunkBuffer createBuffer(String bufferId) {
        DefaultFlowDataChunkBuffer buffer = new DefaultFlowDataChunkBuffer(bufferId);
        buffers.put(bufferId, buffer);
        return buffer;
    }

    /**
     * 获取指定的缓冲区
     *
     * @param bufferId 缓冲区唯一标识
     * @return 缓冲区实例，如果不存在则返回null
     */
    public FlowDataChunkBuffer getBuffer(String bufferId) {
        return buffers.get(bufferId);
    }

    /**
     * 获取或创建缓冲区
     * 如果缓冲区不存在，则创建新的缓冲区
     *
     * @param bufferId 缓冲区唯一标识
     * @return 缓冲区实例
     */
    public FlowDataChunkBuffer getOrCreateBuffer(String bufferId) {
        FlowDataChunkBuffer buffer = getBuffer(bufferId);
        if (buffer == null) {
            buffer = createBuffer(bufferId);
        }
        return buffer;
    }

    /**
     * 删除指定的缓冲区
     *
     * @param bufferId 缓冲区唯一标识
     */
    public void removeBuffer(String bufferId) {
        FlowDataChunkBuffer buffer = buffers.remove(bufferId);
        if (buffer != null) {
            buffer.close();
        }
    }

    /**
     * 检查缓冲区是否存在
     *
     * @param bufferId 缓冲区唯一标识
     * @return 是否存在
     */
    public boolean hasBuffer(String bufferId) {
        return buffers.containsKey(bufferId);
    }

    /**
     * 获取所有缓冲区
     *
     * @return 缓冲区映射
     */
    public Map<String, FlowDataChunkBuffer> getAllBuffers() {
        return new ConcurrentHashMap<>(buffers);
    }

    /**
     * 关闭所有缓冲区
     */
    public void closeAllBuffers() {
        for (FlowDataChunkBuffer buffer : buffers.values()) {
            buffer.close();
        }
        buffers.clear();
    }

    /**
     * 获取管理器ID
     *
     * @return 管理器ID
     */
    public String getManagerId() {
        return managerId;
    }

    /**
     * 获取缓冲区数量
     *
     * @return 缓冲区数量
     */
    public int getBufferCount() {
        return buffers.size();
    }
} 