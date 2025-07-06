package source.hanger.flow.core.runtime.buffer;

import source.hanger.flow.contract.runtime.channel.FlowDataChunk;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkBuffer;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DefaultFlowDataChunkBuffer 基于生产者消费者模式的缓冲区实现
 *
 * 设计特点：
 * 1. buffer维护数据列表
 * 2. 每个listener维护自己的阻塞队列
 * 3. push时同时放入buffer和所有listener的队列
 * 4. listener在onReceive中阻塞式take，处理速度不同也不用关心
 */
public class DefaultFlowDataChunkBuffer extends FlowDataChunkBuffer {

    private final List<FlowDataChunk> dataList;
    private final List<ListenerInfo> listenerInfos;
    private final AtomicBoolean closed;
    private final AtomicInteger nextListenerId;

    public DefaultFlowDataChunkBuffer(String bufferId) {
        super();
        this.bufferId = bufferId;
        dataList = new ArrayList<>();
        listenerInfos = new ArrayList<>();
        closed = new AtomicBoolean(false);
        nextListenerId = new AtomicInteger(0);
    }

    @Override
    public void push(FlowDataChunk flowDataChunk) {
        if (!closed.get()) {
            // 添加到buffer的数据列表
            synchronized (dataList) {
                dataList.add(flowDataChunk);
            }
            // 同时放入每个listener的阻塞队列
            synchronized (listenerInfos) {
                for (ListenerInfo listenerInfo : listenerInfos) {
                    listenerInfo.dataQueue.offer(flowDataChunk);
                }
            }
        }
    }

    @Override
    public void onReceive(FlowDataChunkListener listener) {
        if (!closed.get()) {
            // 创建监听器信息，包含独立的阻塞队列
            int listenerId = nextListenerId.getAndIncrement();
            ListenerInfo listenerInfo = new ListenerInfo(listenerId, listener);
            // 添加监听器
            synchronized (listenerInfos) {
                listenerInfos.add(listenerInfo);
            }
            // 发送历史数据到新listener的队列
            sendHistoryToListener(listenerInfo);
            // 在调用线程中开始消费循环，不额外启动线程
            startConsumerInCurrentThread(listenerInfo);
        }
    }

    /**
     * 发送历史数据到指定listener的队列
     */
    private void sendHistoryToListener(ListenerInfo listenerInfo) {
        synchronized (dataList) {
            for (FlowDataChunk chunk : dataList) {
                listenerInfo.dataQueue.offer(chunk);
            }
        }
    }

    /**
     * 获取数据列表
     */
    public List<FlowDataChunk> getDataList() {
        synchronized (dataList) {
            return new ArrayList<>(dataList);
        }
    }

    /**
     * 在调用线程中开始消费循环，不额外启动线程
     */
    private void startConsumerInCurrentThread(ListenerInfo listenerInfo) {
        // 在调用线程中直接开始消费循环
        try {
            while (!closed.get()) {
                // 阻塞式take，处理速度不同也不用关心
                FlowDataChunk chunk = listenerInfo.dataQueue.take();
                listenerInfo.listener.onReceive(chunk);
                if (chunk.isDone() || chunk.isError() || chunk.isCancelled() || chunk.isTimeout()) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 检查缓冲区是否已关闭
     */
    public boolean isClosed() {
        return closed.get();
    }

    /**
     * 关闭缓冲区
     */
    @Override
    public void close() {
        closed.set(true);
        // 清空数据列表
        synchronized (dataList) {
            dataList.clear();
        }
        // 清空所有listener的队列
        synchronized (listenerInfos) {
            for (ListenerInfo listenerInfo : listenerInfos) {
                listenerInfo.dataQueue.clear();
            }
            listenerInfos.clear();
        }
    }

    /**
     * 监听器信息，包含独立的阻塞队列
     */
    private static class ListenerInfo {
        final int listenerId;
        final FlowDataChunkListener listener;
        final BlockingQueue<FlowDataChunk> dataQueue;

        ListenerInfo(int listenerId, FlowDataChunkListener listener) {
            this.listenerId = listenerId;
            this.listener = listener;
            dataQueue = new LinkedBlockingQueue<>();
        }
    }
}