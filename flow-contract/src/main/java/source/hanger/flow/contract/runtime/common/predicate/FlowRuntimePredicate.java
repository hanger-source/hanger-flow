package source.hanger.flow.contract.runtime.common.predicate;

/**
 * 流程运行时条件判断接口
 * 用于在流程执行过程中进行条件判断，决定流程的流转方向
 */
@FunctionalInterface
public interface FlowRuntimePredicate {
    /**
     * 执行条件判断
     *
     * @param access 流程运行时访问接口，提供上下文数据访问能力
     * @return 条件判断结果，true表示条件满足，false表示条件不满足
     */
    boolean test(FlowRuntimePredicateAccess access);
}