package source.hanger.flow.contract.runtime.flow.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowEnterHandingAccessContext;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowEnterHandlingAccess extends FlowRuntimeAccess<FlowEnterHandingAccessContext> {
    @Override
    FlowEnterHandingAccessContext getContext();
}