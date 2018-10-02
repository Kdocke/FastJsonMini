package com.kdocke.fastjson.parser;

import java.lang.reflect.Type;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/19 - 9:47
 */
public class ParseContext {

    public Object             object;
    public final ParseContext parent;
    public final Object       fieldName;
    public Type type;
    private transient String  path;

    public ParseContext(ParseContext parent, Object object, Object fieldName){
        this.parent = parent;
        this.object = object;
        this.fieldName = fieldName;
    }

}
