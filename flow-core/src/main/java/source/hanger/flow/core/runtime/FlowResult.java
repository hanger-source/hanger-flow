package source.hanger.flow.core.runtime;

import java.io.Serializable;
import java.util.Map;

/**
 * 流程执行结果
 * <p>
 * 作用：
 * - 封装流程执行的最终结果
 * - 包含执行状态、参数、错误信息等
 */
public class FlowResult {
    /**
     * 执行ID
     */
    private final String executionId;
    /**
     * 执行状态
     */
    private final FlowStatus status;
    /**
     * 执行参数
     */
    private final Map<String, Serializable> params;
    /**
     * 异常信息
     */
    private final Exception error;

    public FlowResult(String executionId, FlowStatus status, Map<String, Serializable> params) {
        this(executionId, status, params, null);
    }

    public FlowResult(String executionId, FlowStatus status, Map<String, Serializable> params, Exception error) {
        this.executionId = executionId;
        this.status = status;
        this.params = params;
        this.error = error;
    }

    public String getExecutionId() {
        return executionId;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public Map<String, Serializable> getParams() {
        return params;
    }

    public Exception getError() {
        return error;
    }

    public boolean isSuccess() {
        return status == FlowStatus.SUCCESS;
    }

    public boolean isError() {
        return status == FlowStatus.ERROR;
    }
} 