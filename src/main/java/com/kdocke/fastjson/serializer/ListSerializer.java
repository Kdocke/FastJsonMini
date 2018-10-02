package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * ListSerializer 序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 22:55
 */
public class ListSerializer implements ObjectSerializer {

    public static final ListSerializer instance = new ListSerializer();


    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;

        Type elementType = null;

        List<?> list = (List<?>) object;

        if (list.size() == 0) {
            /** 如果集合对象元素为0, 输出[] */
            out.append("[]");
            return;
        }

        /** 创建当前新的序列化context */
        SerialContext context = serializer.context;
        serializer.setContext(context, object, fieldName, 0);

        ObjectSerializer itemSerializer = null;
        try {
            out.append('[');
            for (int i = 0, size = list.size(); i < size; ++i) {
                Object item = list.get(i);
                if (i != 0) {
                    out.append(',');
                }

                if (item == null) {
                    out.append("null");
                } else {
                    Class<?> clazz = item.getClass();

                    if (clazz == Integer.class) {
                        out.writeInt(((Integer) item).intValue());
                    } else {
                        itemSerializer = serializer.getObjectWriter(item.getClass());
                        itemSerializer.write(serializer, item, i, elementType, features);
                    }
                }
            }
            out.append(']');

        } finally {
            serializer.context = context;
        }
    }
}
