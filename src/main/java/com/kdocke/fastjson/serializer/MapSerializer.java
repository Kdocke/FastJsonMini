package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * MapSerializer 序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 16:26
 */
public class MapSerializer implements ObjectSerializer {

    public static MapSerializer instance = new MapSerializer();

    public void write(JSONSerializer serializer
            , Object object
            , Object fieldName
            , Type fieldType
            , int features) throws IOException {
        write(serializer, object, fieldName, fieldType, features, false);
    }

    public void write(JSONSerializer serializer
            , Object object
            , Object fieldName
            , Type fieldType
            , int features //
            , boolean unwrapped) throws IOException {
        SerializeWriter out = serializer.out;

        if (object == null) {
            /** 如果 map 是 null, 输出 "null" 字符串 */
            out.writeNull();
            return;
        }

        Map<?, ?> map = (Map<?, ?>) object;

        SerialContext parent = serializer.context;
        /** 创建当前新的序列化 context */
        serializer.setContext(parent, object, fieldName, 0);
        try {
            if (!unwrapped) {
                out.write('{');
            }

            Class<?> preClazz = null;
            ObjectSerializer preWriter = null;

            boolean first = true;

            for (Map.Entry entry : map.entrySet()){
                Object value = entry.getValue();
                Object entryKey = entry.getKey();

                if (entryKey instanceof String){
                    String key = (String) entryKey;

                    /** 如果不是第一个属性字段增加分隔符 */
                    if (!first) {
                        out.write(',');
                    }

                    /** 输出key */
                    out.writeFieldName(key, true);
                }

                first = false;

                if (value == null){
                    /** 如果value为空，输出空值 */
                    out.writeNull();
                    continue;
                }

                Class<?> clazz = value.getClass();

                if (clazz != preClazz){
                    preClazz = clazz;
                    preWriter = serializer.getObjectWriter(clazz);
                }

                /** 根据value类型的序列化器 序列化value */
                preWriter.write(serializer, value, entryKey, null, features);
            }
        } finally {
            serializer.context = parent;
        }

        if (!unwrapped) {
            out.write('}');
        }
    }

}
