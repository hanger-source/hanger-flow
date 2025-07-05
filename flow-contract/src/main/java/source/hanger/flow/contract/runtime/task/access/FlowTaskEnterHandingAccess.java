package source.hanger.flow.contract.runtime.task.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowEnterHandingAccessContext;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowTaskEnterHandingAccess extends FlowRuntimeAccess<FlowEnterHandingAccessContext> {
    /**
     *
     **/
    FlowEnterHandingAccessContext getContext();
}