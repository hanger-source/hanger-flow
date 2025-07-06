package source.hanger.flow.core.runtime.execution;

import source.hanger.flow.core.runtime.status.FlowStepStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程执行状态跟踪
 * <p>
 * 作用：
 *   - 跟踪流程执行过程中各个步骤的状态
 *   - 提供状态查询和监控功能
 * <p>
 * 设计说明：
 *   - executionId唯一标识本次执行
 *   - flowName记录流程名称
 *   - stepStatuses跟踪每个步骤的状态
 *   - 线程安全，支持并发访问
 */
public class FlowExecutionState {
    /** 执行ID */
    private final String executionId;
    /** 流程名称 */
    private final String flowName;
    /** 步骤状态映射 */
    private final Map<String, FlowStepStatus> stepStatuses = new ConcurrentHashMap<>();

    public FlowExecutionState(String executionId, String flowName) {
        this.executionId = executionId;
        this.flowName = flowName;
    }

    public void updateStepStatus(String stepName, FlowStepStatus status) {
        stepStatuses.put(stepName, status);
    }

    public FlowStepStatus getStepStatus(String stepName) {
        return stepStatuses.getOrDefault(stepName, FlowStepStatus.PENDING);
    }

    public Map<String, FlowStepStatus> getAllStepStatuses() {
        return new ConcurrentHashMap<>(stepStatuses);
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getFlowName() {
        return flowName;
    }

    public boolean isAllStepsCompleted() {
        return stepStatuses.values().stream()
            .allMatch(status -> status == FlowStepStatus.COMPLETED || status == FlowStepStatus.ERROR);
    }

    public boolean hasError() {
        return stepStatuses.values().stream()
            .anyMatch(status -> status == FlowStepStatus.ERROR);
    }
} 