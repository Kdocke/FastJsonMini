package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * StringCodec 序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/21 - 9:02
 */
public class StringCodec implements ObjectSerializer {

    public static StringCodec instance = new StringCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
            throws IOException {
        write(serializer, (String) object);
    }

    public void write(JSONSerializer serializer, String value) {
        SerializeWriter out = serializer.out;

        out.writeString(value);
    }

}
