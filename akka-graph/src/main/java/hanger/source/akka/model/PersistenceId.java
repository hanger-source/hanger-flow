package hanger.source.akka.model;

/**
 * PersistenceId 用于唯一标识图实例的持久化标识。
 * 典型用法：作为 Akka 持久化 Actor 的 persistenceId 字段，实现状态快照与恢复。
 */
public class PersistenceId {
    private final String id;

    /**
     * 构造函数。
     * @param id 唯一标识字符串
     */
    public PersistenceId(String id) {
        this.id = id;
    }
    /**
     * 获取持久化ID。
     */
    public String id() {
        return id;
    }
} 