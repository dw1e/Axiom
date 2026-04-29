package me.dw1e.axiom.misc.evicting;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class EvictingMap<K, V> extends HashMap<K, V> {

    private final int size;
    private final Deque<K> storedKeys = new ArrayDeque<>();

    public EvictingMap(int size) {
        this.size = size;
    }

    @Override
    public boolean remove(Object key, Object value) {
        storedKeys.remove(key);
        return super.remove(key, value);
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (!storedKeys.contains(key) || !get(key).equals(value)) {
            checkAndRemove();
        }

        return super.putIfAbsent(key, value);
    }

    @Override
    public V put(K key, V value) {
        checkAndRemove();

        storedKeys.addLast(key);
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        storedKeys.clear();
        super.clear();
    }

    @Override
    public V remove(Object key) {
        storedKeys.remove(key);
        return super.remove(key);
    }

    private void checkAndRemove() {
        if (storedKeys.size() >= size) {
            K key = storedKeys.removeFirst();

            remove(key);
        }
    }

}
