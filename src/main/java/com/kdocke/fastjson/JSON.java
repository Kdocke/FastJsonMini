package com.kdocke.fastjson;

import com.kdocke.fastjson.parser.DefaultJSONParser;
import com.kdocke.fastjson.parser.ParserConfig;
import com.kdocke.fastjson.serializer.JSONSerializer;
import com.kdocke.fastjson.serializer.SerializeWriter;

/**
 * Json 解析类，里面包含了主要的 Json 解析方法
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/14 - 9:06
 */
public class JSON implements JSONAware {

    public static int DEFAULT_PARSER_FEATURE = 0;
    public static int DEFAULT_GENERATE_FEATURE = 0;

    /**
     * 把 JSON 文本 parse 为 JSONObject 或者 JSONArray
     * @param text json串
     * @return
     */
    public static Object parse(String text){
        return parse(text, DEFAULT_PARSER_FEATURE);
    }

    /**
     * 把 JSON 文本 parse 为 JSONObject 或者 JSONArray
     * @param text json串
     * @param features 特征、显示效果
     * @return
     */
    public static Object parse(String text, int features) {
        return parse(text, ParserConfig.getGlobalInstance(), features);
    }

    public static Object parse(String text, ParserConfig config, int features) {
        if(text == null){
            return null;
        }

        //初始化 DefaultJSONParser
        DefaultJSONParser parser = new DefaultJSONParser(text, config, features);
        Object value = parser.parse();

        parser.close();

        return value;
    }

    public String toString() {
        return toJSONString();
    }

    /**
     * 将指定 object 序列化成 json 字符串
     * @return
     */
    public String toJSONString() {
        // 构造序列化输出器
        SerializeWriter out = new SerializeWriter();
        try {
            new JSONSerializer(out).write(this);
            return out.toString();
        }finally {
            out.close();
        }
    }
}
