package source.hanger.flow.contract.runtime.flow.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowEnterHandingAccessContext;

/**
 * 流程进入处理访问接口
 * 继承通用上下文，提供流程进入处理时的上下文访问能力
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowEnterHandlingAccess extends FlowRuntimeAccess<FlowEnterHandingAccessContext> {
    /**
     * 获取流程进入处理上下文
     * 
     * @return 流程进入处理上下文
     */
    @Override
    FlowEnterHandingAccessContext getContext();
}