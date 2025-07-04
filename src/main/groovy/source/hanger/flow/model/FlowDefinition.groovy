// src/main/groovy/com/example/flow/model/FlowDefinition.groovy
package source.hanger.flow.model

import java.util.concurrent.ConcurrentHashMap

class FlowDefinition {
    public String name
    public String description
    public Closure<?> onEnterLogic // 流程进入时的逻辑
    public Node startNode // 流程的起始节点
    public Map<String, Node> nodes = new ConcurrentHashMap<>() // 所有节点 (task, entry, parallel)

    String toString() { "Flow(name='$name', nodes=${nodes.keySet()})" }
}
