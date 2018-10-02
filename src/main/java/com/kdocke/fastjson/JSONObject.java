package com.kdocke.fastjson;

import java.io.Serializable;
import java.util.*;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/19 - 15:19
 */
public class JSONObject extends JSON implements Map<String, Object>, Cloneable, Serializable{
    private static final long         serialVersionUID         = 1L;
    private static final int          DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<String, Object> map;

    public JSONObject(){
        this(DEFAULT_INITIAL_CAPACITY, false);
    }

    public JSONObject(boolean ordered){
        this(DEFAULT_INITIAL_CAPACITY, ordered);
    }

    public JSONObject(int initialCapacity, boolean ordered){
        if (ordered) {
            map = new LinkedHashMap<>(initialCapacity);
        } else {
            map = new HashMap<>(initialCapacity);
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object get(Object key) {
        Object val = map.get(key);

        if (val == null && key instanceof Number) {
            val = map.get(key.toString());
        }

        return val;
    }

    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    public Collection<Object> values() {
        return map.values();
    }

    public Map<String, Object> getInnerMap() {
        return this.map;
    }

    public void clear() {
        map.clear();
    }

}
