package source.hanger.flow.util

import source.hanger.flow.contract.runtime.common.FlowRuntimePredicate
import source.hanger.flow.contract.runtime.common.FlowRuntimeExecuteAccess
import source.hanger.flow.contract.runtime.common.FlowClosure

import static groovy.lang.Closure.DELEGATE_FIRST

/**
 * 闭包工具类
 * 提供闭包相关的工具方法
 */
class ClosureUtils {

    /** 始终返回true的闭包 */
    public static final Closure<?> TRUE = { true }

    /**
     * 将闭包转换为FlowRuntimePredicate
     */
    static FlowRuntimePredicate toFlowRuntimePredicate(Closure<?> closure) {
        return new FlowRuntimePredicate() {
            @Override
            boolean test(FlowRuntimeExecuteAccess access) {
                closure.delegate = access
                closure.resolveStrategy = DELEGATE_FIRST
                closure.call()
            }
        }
    }

    /**
     * 将闭包转换为FlowClosure
     */
    static FlowClosure toFlowClosure(Closure<?> closure) {
        return new FlowClosure() {
            @Override
            void call(FlowRuntimeExecuteAccess access) {
                closure.delegate = access
                closure.resolveStrategy = DELEGATE_FIRST
                closure.call()
            }
        }
    }
} 