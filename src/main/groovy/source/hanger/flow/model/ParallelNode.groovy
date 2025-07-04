package source.hanger.flow.model

import java.util.concurrent.ConcurrentHashMap

class ParallelNode implements Node {
    public String name
    public String description
    public Map<String, Branch> branches = new ConcurrentHashMap<>() // 各个并行分支
    public Transition mergeTransition // 所有分支完成后汇聚的流转
    public List<Transition> transitions = [] // 成功后的流转

    String getName() { name }
    String getDescription() { description }

    @Override
    void addTransition(Transition transition) {
        transitions.add(transition)
    }

    String toString() { "Parallel(name='$name', branches=${branches.keySet()})" }
}