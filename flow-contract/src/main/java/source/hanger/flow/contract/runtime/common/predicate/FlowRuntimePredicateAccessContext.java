package source.hanger.flow.contract.runtime.common.predicate;

import java.io.Serial;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccessContext;

/**
 * 流程运行时条件判断访问上下文
 * 继承自FlowRuntimeAccessContext，专门用于条件判断时的上下文数据存储
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class FlowRuntimePredicateAccessContext extends FlowRuntimeAccessContext {

    @Serial
    private static final long serialVersionUID = -2845557246693215069L;
}
