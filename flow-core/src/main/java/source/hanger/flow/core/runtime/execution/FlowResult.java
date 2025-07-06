package source.hanger.flow.core.runtime.execution;

import org.apache.commons.lang3.SerializationUtils;
import source.hanger.flow.core.runtime.status.FlowStatus;

import java.io.Serializable;
import java.util.Map;

import static source.hanger.flow.core.runtime.status.FlowStatus.*;

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
     * context属性
     */
    private final Map<String, Object> attributes;
    /**
     * 异常信息
     */
    private final Exception error;

    private final String stepName;

    public FlowResult(String executionId, String stepName, FlowStatus status, Map<String, Object> attributes) {
        this(executionId, stepName, status, attributes, null);
    }

    public FlowResult(String executionId, String stepName, FlowStatus status, Map<String, Object> attributes,
        Exception error) {
        this.executionId = executionId;
        this.stepName = stepName;
        this.status = status;
        this.attributes = attributes;
        this.error = error;
    }

    public static FlowResult success(FlowExecutionContext flowExecutionContext, String stepName) {
        return new FlowResult(flowExecutionContext.getExecutionId(), stepName, SUCCESS,
            flowExecutionContext.getCloneAttributes());
    }

    /**
     * 创建错误结果
     *
     * @param error 错误信息
     * @return FlowResult实例
     */
    public static FlowResult error(FlowExecutionContext context, Exception error, String stepName) {
        return new FlowResult(context.getExecutionId(), stepName, ERROR, context.getCloneAttributes(), error);
    }

    public String getExecutionId() {
        return executionId;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Exception getError() {
        return error;
    }

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    public boolean isError() {
        return status == ERROR;
    }

    public String getStepName() {
        return stepName;
    }
}