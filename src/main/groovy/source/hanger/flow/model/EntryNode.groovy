package source.hanger.flow.model

class EntryNode implements Node {
    String name
    String description
    // 入口点内部的第一个实际逻辑（通常是一个任务）
    // 为了简化，这里暂时不具体建模内部结构，只表示一个入口点
    // 实际实现会像 FlowDefinition 一样，有内部的 task, entry, parallel 列表
    TaskNode entryStartTask // 入口点内部的第一个任务 (简化模型)

    public List<Transition> transitions = [] // 成功后的流转

    String getName() { name }
    String getDescription() { description }

    @Override
    void addTransition(Transition transition) {
        transitions.add(transition)
    }

    String toString() { "Entry(name='$name')" }
}