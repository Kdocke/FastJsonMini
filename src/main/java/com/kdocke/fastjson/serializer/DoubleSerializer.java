package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Double类型序列化
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/25 - 21:53
 */
public class DoubleSerializer implements ObjectSerializer {

    public final static DoubleSerializer instance      = new DoubleSerializer();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;

        if (object == null) {
            out.writeNull();
            return;
        }

        double doubleValue = ((Double) object).doubleValue();
        if (Double.isNaN(doubleValue) //
                || Double.isInfinite(doubleValue)) {
            out.writeNull();
        } else {

            out.writeDouble(doubleValue);

        }
    }
}
