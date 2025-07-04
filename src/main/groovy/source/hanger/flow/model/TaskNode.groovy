package source.hanger.flow.model

class TaskNode implements Node {
    public String name
    public String description
    public Closure<?> runLogic // 任务执行逻辑
    public List<Transition> transitions = [] // 成功后的流转
    public Transition errorTransition // 错误处理流转

    String getName() { name }
    String getDescription() { description }

    @Override
    void addTransition(Transition transition) {
        transitions.add(transition)
    }

    String toString() { "Task(name='$name')" }
}