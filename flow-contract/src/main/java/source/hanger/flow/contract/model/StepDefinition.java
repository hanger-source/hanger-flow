package source.hanger.flow.contract.model;

/**
 * 流程步骤定义接口
 * 流程中所有可被引用和流转的基本单元
 * 定义了流程步骤的基本属性和行为
 */
public interface StepDefinition {
    /**
     * 获取步骤名称
     * 
     * @return 步骤名称
     */
    String getName();

    /**
     * 获取步骤描述
     * 
     * @return 步骤描述信息
     */
    String getDescription();

    /**
     * 添加流转条件
     * 
     * @param transition 流转条件
     */
    void addTransition(Transition transition);

    /**
     * 设置错误处理流转条件
     * 
     * @param transition 错误处理流转条件
     */
    void setErrorTransition(Transition transition);
}