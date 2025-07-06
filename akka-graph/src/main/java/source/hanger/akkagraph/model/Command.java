package source.hanger.akkagraph.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Command 封装节点执行后的路由和状态更新信息。
 * 用于节点动作返回下一个节点ID和状态。
 */
public record Command(String gotoNode, Map<String, Object> update) implements Serializable {
    @Serial
    private static final long serialVersionUID = -7628680006706682635L;

    /**
     * 构造函数。
     *
     * @param gotoNode 下一个节点ID
     * @param update   状态更新Map
     */
    public Command {
    }

    /**
     * 获取下一个节点ID。
     */
    @Override
    public String gotoNode() {
        return gotoNode;
    }

    /**
     * 获取状态更新Map。
     */
    @Override
    public Map<String, Object> update() {
        return update;
    }
} 