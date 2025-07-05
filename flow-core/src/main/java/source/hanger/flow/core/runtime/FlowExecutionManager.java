package source.hanger.flow.core.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程执行管理器
 * <p>
 * 负责流程执行状态的管理和监控，包括：
 * - 执行状态跟踪
 * - 执行结果缓存
 * - 状态查询和监控
 * 
 * 设计说明：
 * - 作为核心运行时组件，提供统一的状态管理
 * - 支持并发访问，线程安全
 * - 便于扩展监控和统计功能
 */
public class FlowExecutionManager {
    
    /**
     * 流程执行状态跟踪
     */
    private final Map<String, FlowExecutionState> executionStates = new ConcurrentHashMap<>();
    
    /**
     * 流程执行结果缓存
     */
    private final Map<String, FlowResult> resultCache = new ConcurrentHashMap<>();
    
    /**
     * 更新执行状态
     * @param executionId 执行ID
     * @param flowName    流程名称
     * @param stepName    步骤名称
     * @param status      步骤状态
     */
    public void updateStepStatus(String executionId, String flowName, String stepName, FlowStepStatus status) {
        FlowExecutionState state = executionStates.computeIfAbsent(executionId, 
            id -> new FlowExecutionState(executionId, flowName));
        state.updateStepStatus(stepName, status);
    }
    
    /**
     * 缓存执行结果
     * @param executionId 执行ID
     * @param result      执行结果
     */
    public void cacheExecutionResult(String executionId, FlowResult result) {
        resultCache.put(executionId, result);
    }
    
    /**
     * 获取执行状态
     * @param executionId 执行ID
     * @return 执行状态
     */
    public FlowExecutionState getExecutionState(String executionId) {
        return executionStates.get(executionId);
    }
    
    /**
     * 获取执行结果
     * @param executionId 执行ID
     * @return 执行结果
     */
    public FlowResult getExecutionResult(String executionId) {
        return resultCache.get(executionId);
    }
    
    /**
     * 清理执行数据
     * @param executionId 执行ID
     */
    public void cleanupExecution(String executionId) {
        executionStates.remove(executionId);
        resultCache.remove(executionId);
    }
    
    /**
     * 获取所有执行状态
     * @return 执行状态映射
     */
    public Map<String, FlowExecutionState> getAllExecutionStates() {
        return new ConcurrentHashMap<>(executionStates);
    }
    
    /**
     * 获取所有执行结果
     * @return 执行结果映射
     */
    public Map<String, FlowResult> getAllExecutionResults() {
        return new ConcurrentHashMap<>(resultCache);
    }
} 