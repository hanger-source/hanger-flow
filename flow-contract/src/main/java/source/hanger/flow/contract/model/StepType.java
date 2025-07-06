package source.hanger.flow.contract.model;

/**
 * 步骤类型枚举
 * 定义了流程中所有可用的步骤类型
 * 
 * @author fuhangbo.hanger.uhfun
 */
public enum StepType {
    
    /**
     * 任务步骤 - 执行具体的业务逻辑
     */
    TASK("task", "任务步骤"),
    
    /**
     * 并行步骤 - 同时执行多个分支
     */
    PARALLEL("parallel", "并行步骤"),
    
    /**
     * 异步步骤 - 异步执行分支，不阻塞主流程
     */
    ASYNC("async", "异步步骤"),
    
    /**
     * 条件步骤 - 根据条件进行分支
     */
    CONDITION("condition", "条件步骤"),
    
    /**
     * 等待步骤 - 等待外部事件或条件
     */
    WAIT("wait", "等待步骤"),
    
    /**
     * 子流程步骤 - 调用其他流程
     */
    SUBFLOW("subflow", "子流程步骤"),
    
    /**
     * 脚本步骤 - 执行脚本逻辑
     */
    SCRIPT("script", "脚本步骤"),
    
    /**
     * 网关步骤 - 流程控制节点
     */
    GATEWAY("gateway", "网关步骤");

    private final String code;
    private final String description;

    StepType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取步骤类型
     * 
     * @param code 步骤类型代码
     * @return 步骤类型枚举，如果不存在则返回null
     */
    public static StepType fromCode(String code) {
        for (StepType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 检查是否为任务类型步骤
     * 
     * @return true表示是任务类型
     */
    public boolean isTaskType() {
        return this == TASK || this == SCRIPT;
    }

    /**
     * 检查是否为控制类型步骤
     * 
     * @return true表示是控制类型
     */
    public boolean isControlType() {
        return this == PARALLEL || this == ASYNC || this == CONDITION || this == GATEWAY;
    }

    /**
     * 检查是否为等待类型步骤
     * 
     * @return true表示是等待类型
     */
    public boolean isWaitType() {
        return this == WAIT;
    }
} 