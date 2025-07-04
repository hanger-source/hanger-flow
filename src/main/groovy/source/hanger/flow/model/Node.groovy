package source.hanger.flow.model

// 流程中所有可被引用和流转的基本单元
interface Node {
    String getName()
    String getDescription()
    void addTransition(Transition transition)
//    void addErrorTransition(Transition transition)
}