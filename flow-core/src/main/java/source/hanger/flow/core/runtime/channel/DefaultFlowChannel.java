package source.hanger.flow.core.runtime.channel;

import source.hanger.flow.contract.runtime.channel.FlowDataChunkBuffer;
import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;
import source.hanger.flow.core.runtime.buffer.EmptyFlowDataChunkBuffer;
import source.hanger.flow.core.runtime.buffer.FlowBufferManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DefaultFlowChannel 提供基于FlowBufferManager的channel实现
 * 使用者通过acquireBuffer方法获取缓冲区，然后进行数据推送和监听
 */
public class DefaultFlowChannel implements FlowStreamingChannel {

    private final String channelName;
    private final FlowBufferManager bufferManager;
    private final AtomicBoolean closed;
    private final ConcurrentHashMap<String, FlowDataChunkBuffer> buffers;

    public DefaultFlowChannel(String channelName) {
        this.channelName = channelName;
        bufferManager = new FlowBufferManager(channelName + "-manager");
        closed = new AtomicBoolean(false);
        buffers = new ConcurrentHashMap<>();
    }

    @Override
    public FlowDataChunkBuffer acquireBuffer(String bufferId) {
        if (closed.get()) {
            throw new IllegalStateException("Channel is closed");
        }
        return buffers.computeIfAbsent(bufferId, bufferManager::createBuffer);
    }

    @Override
    public FlowDataChunkBuffer getBuffer(String bufferId) {
        FlowDataChunkBuffer buffer = buffers.get(bufferId);
        if (buffer == null) {
            return new EmptyFlowDataChunkBuffer(bufferId);
        }
        return buffer;
    }

    @Override
    public void releaseBuffer(String bufferId) {
        if (!closed.get()) {
            FlowDataChunkBuffer buffer = buffers.remove(bufferId);
            if (buffer != null) {
                bufferManager.removeBuffer(bufferId);
            }
        }
    }

    @Override
    public boolean hasBuffer(String bufferId) {
        return buffers.containsKey(bufferId);
    }

    public String getChannelName() {
        return channelName;
    }

    public void close() {
        closed.set(true);

        // 释放所有缓冲区
        for (String bufferId : buffers.keySet()) {
            releaseBuffer(bufferId);
        }
        buffers.clear();

        bufferManager.closeAllBuffers();
    }

    /**
     * 获取缓冲区管理器
     *
     * @return FlowBufferManager实例
     */
    public FlowBufferManager getBufferManager() {
        return bufferManager;
    }

    /**
     * 检查channel是否已关闭
     *
     * @return 是否已关闭
     */
    public boolean isClosed() {
        return closed.get();
    }
}

