package com.kdocke.fastjson.serializer;

import com.kdocke.fastjson.JSONException;

import java.io.IOException;
import java.util.IdentityHashMap;

/**
 * JSON 序列化器
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 10:49
 */
public class JSONSerializer {

    protected SerialContext                          context;
    protected final SerializeConfig                  config;
    public final SerializeWriter                     out;

    protected IdentityHashMap<Object, SerialContext> references  = null;

    public JSONSerializer(SerializeWriter out){
        this(out, SerializeConfig.getGlobalInstance());
    }

    public JSONSerializer(SerializeWriter out, SerializeConfig config) {
        this.out = out;
        this.config = config;
    }

    public final void write(Object object) {
        if (object == null) {
            /** 如果对象为空，直接输出 "null" 字符串 */
            out.writeNull();
            return;
        }

        // 即判断object为：JSONObject 还是 JSONArray
        Class<?> clazz = object.getClass();
        /** 根据对象的Class类型查找具体序列化实例 */
        ObjectSerializer writer = getObjectWriter(clazz);
        try {
            /** 使用具体serializer实例处理对象 */
            writer.write(this, object, null, null, 0);
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
    }

    public ObjectSerializer getObjectWriter(Class<?> clazz) {
        return config.getObjectWriter(clazz);
    }

    public void setContext(SerialContext parent, Object object, Object fieldName, int features) {
        this.setContext(parent, object, fieldName, features, 0);
    }

    public void setContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures) {

        this.context = new SerialContext(parent, object, fieldName, features, fieldFeatures);
        if (references == null) {
            references = new IdentityHashMap<Object, SerialContext>();
        }
        this.references.put(object, context);
    }

}
