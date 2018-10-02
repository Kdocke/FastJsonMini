package com.kdocke.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * ObjectSerializer 序列化接口
 * 真正序列化对象的时候是由具体ObjectSerializer实例完成
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 10:58
 */
public interface ObjectSerializer {

    void write(JSONSerializer serializer, /** json序列化实例 */
               Object object, /** 待序列化的对象*/
               Object fieldName, /** 待序列化字段*/
               Type fieldType, /** 待序列化字段类型 */
               int features) throws IOException;

}
