package source.hanger.flow.contract.runtime.wrapper;

import source.hanger.flow.contract.runtime.channel.FlowDataChunk;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkBuffer;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkListener;
import source.hanger.flow.contract.runtime.channel.FlowStreamingChannel;

/**
 * 包装 FlowStreamingChannel，强约束 buffer 用法：
 * acquireBuffer 只能 onReceive，getBuffer 只能 push，反之抛异常。
 */
public class FlowStreamingChannelSafeWrapper implements FlowStreamingChannel {
    private final FlowStreamingChannel delegate;

    public FlowStreamingChannelSafeWrapper(FlowStreamingChannel delegate) {
        this.delegate = delegate;
    }

    @Override
    public FlowDataChunkBuffer acquireBuffer(String id) {
        return new PushOnlyBufferWrapper(delegate.acquireBuffer(id));
    }

    @Override
    public FlowDataChunkBuffer getBuffer(String id) {
        return new ReceiveOnlyBufferWrapper(delegate.getBuffer(id));
    }

    @Override
    public void releaseBuffer(String bufferId) {
        delegate.releaseBuffer(bufferId);
    }

    @Override
    public boolean hasBuffer(String bufferId) {
        return delegate.hasBuffer(bufferId);
    }

    /**
     * 只允许 onReceive，不允许 push
     */
    public static class ReceiveOnlyBufferWrapper extends FlowDataChunkBuffer {
        private final FlowDataChunkBuffer delegate;

        public ReceiveOnlyBufferWrapper(FlowDataChunkBuffer delegate) {
            this.delegate = delegate;
            bufferId = delegate.getBufferId();
            sourceStep = delegate.getSourceStep();
            sourceFlow = delegate.getSourceFlow();
        }

        @Override
        public void push(FlowDataChunk flowDataChunk) {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 push！");
        }

        @Override
        public void onReceive(FlowDataChunkListener listener) {
            delegate.onReceive(listener);
        }

        @Override
        public void pushFragment(String data) {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushFragment！");
        }

        @Override
        public void pushFragment(byte[] data) {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushFragment！");
        }

        @Override
        public void pushFragment(Object data) {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushFragment！");
        }

        @Override
        public void pushFragment(Object data, source.hanger.flow.contract.runtime.channel.DataType dataType) {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushFragment！");
        }

        @Override
        public void pushDone() {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushDone！");
        }

        @Override
        public void pushError(Throwable error) {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushError！");
        }

        @Override
        public void pushCancelled() {
            throw new UnsupportedOperationException("acquireBuffer() 只能用于 onReceive，不允许 pushCancelled！");
        }

        @Override
        public void close() {delegate.close();}
    }

    /**
     * 只允许 push，不允许 onReceive
     */
    public static class PushOnlyBufferWrapper extends FlowDataChunkBuffer {
        private final FlowDataChunkBuffer delegate;

        public PushOnlyBufferWrapper(FlowDataChunkBuffer delegate) {
            this.delegate = delegate;
            bufferId = delegate.getBufferId();
            sourceStep = delegate.getSourceStep();
            sourceFlow = delegate.getSourceFlow();
        }

        @Override
        public void push(FlowDataChunk flowDataChunk) {
            delegate.push(flowDataChunk);
        }

        @Override
        public void onReceive(FlowDataChunkListener listener) {
            throw new UnsupportedOperationException("getBuffer() 只能用于 push，不允许 onReceive 监听！");
        }

        @Override
        public void pushFragment(String data) {delegate.pushFragment(data);}

        @Override
        public void pushFragment(byte[] data) {delegate.pushFragment(data);}

        @Override
        public void pushFragment(Object data) {delegate.pushFragment(data);}

        @Override
        public void pushFragment(Object data,
            source.hanger.flow.contract.runtime.channel.DataType dataType) {delegate.pushFragment(data, dataType);}

        @Override
        public void pushDone() {delegate.pushDone();}

        @Override
        public void pushError(Throwable error) {delegate.pushError(error);}

        @Override
        public void pushCancelled() {delegate.pushCancelled();}

        @Override
        public void close() {delegate.close();}
    }
} 