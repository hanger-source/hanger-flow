package source.hanger.flow.core.runtime.buffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source.hanger.flow.contract.runtime.channel.FlowDataChunk;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkBuffer;
import source.hanger.flow.contract.runtime.channel.FlowDataChunkListener;

/**
 * 空Buffer实现，所有操作无效，仅日志提示
 */
public class EmptyFlowDataChunkBuffer extends FlowDataChunkBuffer {
    private static final Logger log = LoggerFactory.getLogger(EmptyFlowDataChunkBuffer.class);

    public EmptyFlowDataChunkBuffer(String bufferId) {
        super();
        this.bufferId = bufferId;
    }

    @Override
    public void push(FlowDataChunk flowDataChunk) {
        // do nothing
    }

    @Override
    public void onReceive(FlowDataChunkListener listener) {
        log.info("Buffer {} is empty, no data will be sent to listener {}", bufferId, listener);
    }

    @Override
    public void close() {
        // do nothing
    }
}
