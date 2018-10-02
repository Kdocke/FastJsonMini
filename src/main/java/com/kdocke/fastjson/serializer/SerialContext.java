package com.kdocke.fastjson.serializer;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 16:55
 */
public class SerialContext {

    public final SerialContext parent;
    public final Object        object;
    public final Object        fieldName;
    public final int           features;

    public SerialContext(SerialContext parent, Object object, Object fieldName, int features, int fieldFeatures){
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
        this.features = features;
    }

}
