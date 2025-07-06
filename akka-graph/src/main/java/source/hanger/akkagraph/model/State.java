package source.hanger.akkagraph.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * State 代表图的不可变全局状态，是键值对集合。
 * 每次更新都会返回新的 State 实例，保证状态不可变性。
 */
public record State(Map<String, Object> data) implements Serializable {
    @Serial
    private static final long serialVersionUID = -1707200526422710420L;

    /**
     * 构造函数。
     *
     * @param data 状态数据Map
     */
    public State(Map<String, Object> data) {
        this.data = Map.copyOf(data);
    }

    /**
     * 获取状态数据。
     */
    @Override
    public Map<String, Object> data() {
        return data;
    }

    /**
     * 返回包含新键值对的 State 实例。
     */
    public State with(String key, Object value) {
        Map<String, Object> newData = new HashMap<>(data);
        newData.put(key, value);
        return new State(newData);
    }

    /**
     * 返回包含所有更新的 State 实例。
     */
    public State withAll(Map<String, Object> updates) {
        Map<String, Object> newData = new HashMap<>(data);
        newData.putAll(updates);
        return new State(newData);
    }
} 