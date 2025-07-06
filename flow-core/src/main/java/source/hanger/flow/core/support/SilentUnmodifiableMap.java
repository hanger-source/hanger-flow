package source.hanger.flow.core.support;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 静默不可变Map装饰器
 * 在DSL中看起来可修改，但实际修改操作会静默忽略
 *
 * @author fuhangbo.hanger.uhfun
 **/
public record SilentUnmodifiableMap<K, V>(Map<K, V> delegate) implements Map<K, V> {

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return delegate.get(key);
    }

    @Override
    public V put(K key, V value) {
        // 静默忽略修改操作
        return null;
    }

    @Override
    public V remove(Object key) {
        // 静默忽略删除操作
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        // 静默忽略批量修改操作
    }

    @Override
    public void clear() {
        // 静默忽略清空操作
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return delegate.getOrDefault(key, defaultValue);
    }
}
