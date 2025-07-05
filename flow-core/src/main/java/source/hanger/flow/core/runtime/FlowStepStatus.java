package source.hanger.flow.core.runtime;

/**
 * 流程步骤执行状态枚举
 * <p>
 * 作用：
 *   - 定义流程步骤执行的各种状态
 *   - 用于步骤状态跟踪和监控
 */
public enum FlowStepStatus {
    /** 等待执行 */
    PENDING,
    /** 正在执行 */
    RUNNING,
    /** 执行完成 */
    COMPLETED,
    /** 执行失败 */
    ERROR
} 