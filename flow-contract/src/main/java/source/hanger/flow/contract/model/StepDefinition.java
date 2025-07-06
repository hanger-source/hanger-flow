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

    /**
     * 是否支持流式输出（fragment/done/error）
     *
     * @return true表示支持流式输出，false为传统模式
     */
    default boolean isStreamingSupported() {
        return false;
    }

    /**
     * 获取步骤输出的数据类型（如String.class、POJO.class等）
     *
     * @return 输出类型Class
     */
    default Class<?> getOutputType() {
        return Object.class;
    }

    /**
     * 获取步骤类型
     *
     * @return 步骤类型枚举
     */
    StepType getStepType();
}