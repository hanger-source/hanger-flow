package source.hanger.flow.contract.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程步骤定义的抽象基类
 * 提供了流程步骤的基本属性和行为，包括名称、描述、流转条件和错误处理
 * 
 * @author fuhangbo.hanger.uhfun
 **/
public abstract class AbstractStepDefinition implements StepDefinition {
    /** 步骤名称 */
    protected String name;
    /** 步骤描述 */
    protected String description;
    /** 流转条件列表，支持多个条件分支 */
    protected List<Transition> transition = new ArrayList<>();
    /** 错误处理流转条件 */
    protected Transition errorTransition;

    @Override
    public void addTransition(Transition transition) {
        this.transition.add(transition);
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 设置步骤名称
     * 
     * @param name 步骤名称
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * 设置步骤描述
     * 
     * @param description 步骤描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取错误处理流转条件
     * 
     * @return 错误处理流转条件
     */
    public Transition getErrorTransition() {
        return errorTransition;
    }

    @Override
    public void setErrorTransition(Transition errorTransition) {
        this.errorTransition = errorTransition;
    }
}
