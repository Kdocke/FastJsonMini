package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * FloatCodec 序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/24 - 15:54
 */
public class FloatCodec implements ObjectSerializer {

    public static FloatCodec instance = new FloatCodec();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        float floatValue = ((Float) object).floatValue();

        out.writeFloat(floatValue);
    }
}
