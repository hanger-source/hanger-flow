package source.hanger.flow.contract.model;

import source.hanger.flow.contract.runtime.common.FlowClosure;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程定义（FlowDefinition）
 * <p>
 * 作用：
 *   - 表示整个流程的结构、元信息、全局回调和所有步骤节点
 *   - 是流程DSL解析和运行时的核心数据结构
 * <p>
 * 典型用法：
 *   - 由DSL flow { ... } 语法块生成
 *   - 作为流程引擎的输入模型
 * <p>
 * 设计说明：
 *   - version/name/description为流程元信息
 *   - enterHandlingClosure为流程进入时的全局回调
 *   - errorHandlingClosure为流程全局错误处理回调
 *   - stepDefinitions为流程的所有步骤节点（任务、并行、异步等）
 *   - 支持动态添加步骤
 */
public class FlowDefinition {
    /** 流程版本号 */
    private String version;
    /** 流程名称 */
    private String name;
    /** 流程描述 */
    private String description;
    /**
     * 流程进入时的逻辑（全局onEnter）
     * 只有一次
     */
    private FlowClosure enterHandlingClosure;
    /**
     * 异常处理逻辑（全局onError）
     * 只有一次
     */
    private FlowClosure errorHandlingClosure;
    /**
     * 流程步骤节点列表（任务、并行、异步等）
     */
    private List<StepDefinition> stepDefinitions;

    public FlowDefinition() {
    }

    public FlowDefinition(String name) {
        this.name = name;
    }

    public FlowDefinition(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 添加流程步骤节点
     * @param stepDefinition 步骤节点
     */
    public void addStep(StepDefinition stepDefinition) {
        if (stepDefinitions == null) {
            stepDefinitions = new ArrayList<>();
        }
        stepDefinitions.add(stepDefinition);
    }

    /**
     * 获取流程版本号
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * 设置流程版本号
     * @param version 版本号
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 获取流程名称
     * @return 流程名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置流程名称
     * @param name 流程名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取流程描述
     * @return 流程描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置流程描述
     * @param description 流程描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取流程进入处理闭包
     */
    public FlowClosure getEnterHandlingClosure() {
        return enterHandlingClosure;
    }

    /**
     * 设置流程进入处理闭包
     */
    public void setEnterHandlingClosure(FlowClosure enterHandlingClosure) {
        this.enterHandlingClosure = enterHandlingClosure;
    }

    /**
     * 获取流程错误处理闭包
     */
    public FlowClosure getErrorHandlingClosure() {
        return errorHandlingClosure;
    }

    /**
     * 设置流程错误处理闭包
     */
    public void setErrorHandlingClosure(FlowClosure errorHandlingClosure) {
        this.errorHandlingClosure = errorHandlingClosure;
    }

    /**
     * 获取所有流程步骤节点
     * @return 步骤节点列表
     */
    public List<StepDefinition> getStepDefinitions() {
        if (stepDefinitions == null) {
            stepDefinitions = new ArrayList<>();
        }
        return stepDefinitions;
    }

    public void setStepDefinitions(List<StepDefinition> stepDefinitions) {
        this.stepDefinitions = stepDefinitions;
    }

    // 兼容性方法，保持向后兼容
    public FlowClosure getEnterHandingRunnable() {
        return enterHandlingClosure;
    }

    public void setEnterHandingRunnable(FlowClosure enterHandlingClosure) {
        this.enterHandlingClosure = enterHandlingClosure;
    }

    public FlowClosure getErrorHandingRunnable() {
        return errorHandlingClosure;
    }

    public void setErrorHandingRunnable(FlowClosure errorHandlingClosure) {
        this.errorHandlingClosure = errorHandlingClosure;
    }
}
