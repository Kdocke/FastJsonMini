package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * IntegerCodec 序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/21 - 9:00
 */
public class IntegerCodec implements ObjectSerializer {

    public static IntegerCodec instance = new IntegerCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;

        Number value = (Number) object;
        out.writeInt(value.intValue());

    }

}
