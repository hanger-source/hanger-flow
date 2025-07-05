package source.hanger.flow.contract.model;

// 流程中所有可被引用和流转的基本单元
public interface StepDefinition {
    String getName();

    String getDescription();

    void addTransition(Transition transition);

    void setErrorTransition(Transition transition);
}