package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * BooleanCodec 序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/21 - 8:58
 */
public class BooleanCodec implements ObjectSerializer {

    public final static BooleanCodec instance = new BooleanCodec();

    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;

        /** 当前object转为 Boolean 类型 */
        Boolean value = (Boolean) object;
        if (value.booleanValue()) {
            out.write("true");
        } else {
            out.write("false");
        }
    }

}
