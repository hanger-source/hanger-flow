package source.hanger.flow.contract.runtime.task.access;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;
import source.hanger.flow.contract.runtime.task.context.FlowTaskErrorHandingAccessContext;

/**
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowTaskErrorHandlingAccess extends FlowRuntimeAccess<FlowTaskErrorHandingAccessContext> {
    Throwable getException();

    @Override
    FlowTaskErrorHandingAccessContext getContext();
}