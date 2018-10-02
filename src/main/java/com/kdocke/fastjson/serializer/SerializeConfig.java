package com.kdocke.fastjson.serializer;

import com.kdocke.fastjson.util.IdentityHashMap;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 根据类型查找具体序列化实例，
 * 查找方法基本思想根据 class 类型或者实现接口类型进行匹配查找。
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 10:50
 */
public class SerializeConfig {

    public final static SerializeConfig globalInstance = new SerializeConfig();

    /**
     * <a href="https://segmentfault.com/q/1010000002779228">Java IdentityHashMap 与 HashMap 的区别</a>
     */
    private final IdentityHashMap<Type, ObjectSerializer> serializers;
    private final boolean                                 fieldBased;

    public SerializeConfig() {
        this(IdentityHashMap.DEFAULT_SIZE);
    }

    public SerializeConfig(int tableSize) {
        this(tableSize, false);
    }

    public SerializeConfig(int tableSize, boolean fieldBase) {
        this.fieldBased = fieldBase;
        serializers = new IdentityHashMap<>(tableSize);

        initSerializers();
    }

    private void initSerializers() {
        put(Boolean.class, BooleanCodec.instance);
        put(Integer.class, IntegerCodec.instance);
        put(Float.class, FloatCodec.instance);
        put(Double.class, DoubleSerializer.instance);
        put(String.class, StringCodec.instance);
    }

    public static SerializeConfig getGlobalInstance() {
        return globalInstance;
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return getObjectWriter(clazz, true);
    }

    /**
     * 从内部已经注册查找特定 class 的序列化实例
     * 若没有，则判断具体的序列化类型，并添加到内部表中
     * @param clazz
     * @param create
     * @return
     */
    private ObjectSerializer getObjectWriter(Class<?> clazz, boolean create) {
        /** 首先从内部已经注册查找特定 class 的序列化实例 */
        ObjectSerializer writer = serializers.get(clazz);

        if (writer == null) {
            if (Map.class.isAssignableFrom(clazz)) {
                /** 如果class实现类Map接口，使用MapSerializer序列化 */
                put(clazz, writer = MapSerializer.instance);
            } else if (List.class.isAssignableFrom(clazz)) {
                /** 如果class实现类List接口，使用ListSerializer序列化 */
                put(clazz, writer = ListSerializer.instance);
            }

            if (writer == null) {
                /** 尝试在已注册缓存找到特定class的序列化实例 */
                writer = serializers.get(clazz);
            }
        }
        return writer;
    }

    public boolean put(Type type, ObjectSerializer value) {
        return this.serializers.put(type, value);
    }

}
