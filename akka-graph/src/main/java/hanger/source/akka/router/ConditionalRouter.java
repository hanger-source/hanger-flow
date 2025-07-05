package hanger.source.akka.router;

import hanger.source.akka.model.State;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * ConditionalRouter 根据条件表达式动态路由到不同节点。
 * 支持多分支，常用于 if/else、switch 场景。
 * 典型用法：根据 State 或输出内容选择下一个节点。
 */
public class ConditionalRouter implements IRouter<State> {
    private final Map<String, BiPredicate<State, Object>> conditions;
    private final String defaultNodeId;

    /**
     * 构造函数。
     * @param conditions 分支条件Map，key为节点ID，value为条件谓词
     * @param defaultNodeId 默认节点ID
     */
    public ConditionalRouter(Map<String, BiPredicate<State, Object>> conditions, String defaultNodeId) {
        this.conditions = conditions;
        this.defaultNodeId = defaultNodeId;
    }
    @Override
    public String route(State state, Object output) {
        for (Map.Entry<String, BiPredicate<State, Object>> entry : conditions.entrySet()) {
            if (entry.getValue().test(state, output)) {
                return entry.getKey();
            }
        }
        return defaultNodeId;
    }
} 