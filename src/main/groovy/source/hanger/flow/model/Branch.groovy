package source.hanger.flow.model

class Branch {
    String name
    Node startNode // 分支的起始节点

    String toString() { "Branch(name='$name')" }
}