package source.hanger.flow.contract.runtime.task.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskRunAccessContext;

/**
 * @author fuhangbo.hanger.uhfun
 **/
// 继承通用上下文
public interface FlowTaskRunAccess extends FlowRuntimeAccess<FlowTaskRunAccessContext> {
    /**
     *
     **/
    FlowTaskRunAccessContext getContext();
}