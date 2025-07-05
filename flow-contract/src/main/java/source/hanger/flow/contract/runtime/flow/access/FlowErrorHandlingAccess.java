package source.hanger.flow.contract.runtime.flow.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.flow.context.FlowErrorHandingAccessContext;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowErrorHandlingAccess extends FlowRuntimeAccess<FlowErrorHandingAccessContext> {
    Throwable getException();

    @Override
    FlowErrorHandingAccessContext getContext();
}