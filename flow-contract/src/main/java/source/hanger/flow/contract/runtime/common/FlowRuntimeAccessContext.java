package source.hanger.flow.contract.runtime.common;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流程运行时访问上下文抽象基类
 * 提供了流程执行过程中的上下文数据存储能力
 * 支持序列化，可用于分布式场景
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class FlowRuntimeAccessContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 6473185372366539085L;
    private String executionId;
    private String stepName;

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }
}
