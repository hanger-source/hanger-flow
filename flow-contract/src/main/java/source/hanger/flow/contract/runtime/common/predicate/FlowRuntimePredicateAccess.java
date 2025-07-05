package source.hanger.flow.contract.runtime.common.predicate;

import source.hanger.flow.contract.runtime.common.FlowRuntimeAccess;

/**
 * 流程运行时条件判断访问接口
 * 继承通用上下文。注意：此接口可能不需要额外的方法，
 * 只是为了明确语义和类型安全，防止在条件判断中不小心修改流程状态。
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public interface FlowRuntimePredicateAccess extends FlowRuntimeAccess<FlowRuntimePredicateAccessContext> {
    /**
     * 获取条件判断上下文
     * 
     * @return 条件判断上下文
     */
    FlowRuntimePredicateAccessContext getContext();
}
