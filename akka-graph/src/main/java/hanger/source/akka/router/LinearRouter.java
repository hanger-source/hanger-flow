package hanger.source.akka.router;

import hanger.source.akka.model.State;
import java.util.List;

/**
 * LinearRouter 按顺序依次路由到下一个节点。
 * 常用于线性流程。
 * 典型用法：节点列表依次执行。
 */
public class LinearRouter implements IRouter<State> {
    private final List<String> nodeIds;
    private int currentIndex = 0;

    /**
     * 构造函数。
     * @param nodeIds 节点ID列表
     */
    public LinearRouter(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }
    @Override
    public String route(State state, Object output) {
        if (currentIndex < nodeIds.size() - 1) {
            return nodeIds.get(++currentIndex);
        }
        return nodeIds.get(nodeIds.size() - 1);
    }
} 