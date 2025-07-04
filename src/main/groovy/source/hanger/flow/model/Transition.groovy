package source.hanger.flow.model

class Transition {
    public Closure<Boolean> condition // 流转条件，返回 true/false
    public String targetNodeName // 目标节点名称 (在 FlowDefinition.nodes 中查找)

    String toString() { "Transition(target='$targetNodeName')" }
}