package com.kdocke.fastjson.util;

import com.kdocke.fastjson.JSONArray;
import com.kdocke.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Json 美化工具类
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/24 - 18:23
 */
public class BeautifyJsonUtils {
    // 空格
    private static final char SPACE_CHAR = ' ';

    // 缩进
    private static final int INDENT_SIZE = 2;

    // 深度
    private static int callDepth = 0;

    /**
     * 判断具体执行方法
     * @param object
     * @return
     */
    public static String beautify(Object object){
        if (object instanceof JSONObject){
            return beautify((JSONObject) object);
        }else {
            return beautify((JSONArray) object);
        }
    }

    /**
     * 对 JsonObject 进行输出美化
     * @param jsonObject
     * @return
     */
    public static String beautify(JSONObject jsonObject){
        StringBuilder sb = new StringBuilder();

        sb.append((getIndentString()));
        sb.append("{");
        callDepth++;

        List<Map.Entry<String, Object>> keyValues = new ArrayList<>(jsonObject.getInnerMap().entrySet());
        int size = keyValues.size();
        for (int i = 0; i < size; i++) {
            Map.Entry<String, Object> keyValue = keyValues.get(i);

            String key = keyValue.getKey();
            Object value = keyValue.getValue();

            sb.append("\n");
            sb.append(getIndentString());
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            sb.append(":");

            if(value instanceof JSONObject) {
                sb.append("\n");
                sb.append(beautify((JSONObject) value));
            }else if(value instanceof JSONArray){
                sb.append("\n");
                sb.append(beautify((JSONArray) value));
            }else if(value instanceof String){
                sb.append("\"");
                sb.append(value);
                sb.append("\"");
            }else{
                sb.append(value);
            }

            if(i < size - 1){
                sb.append(",");
            }
        }

        callDepth--;
        sb.append("\n");
        sb.append(getIndentString());
        sb.append("}");

        return sb.toString();
    }

    /**
     * 对 JsonArray 进行输出美化
     * @param jsonArray
     * @return
     */
    public static String beautify(JSONArray jsonArray) {
        StringBuilder sb = new StringBuilder();
        sb.append(getIndentString());
        sb.append("[");
        callDepth++;

        int size = jsonArray.size();
        for (int i = 0; i < size; i++) {
            sb.append("\n");

            Object ele = jsonArray.get(i);
            if(ele instanceof JSONObject){
                sb.append(beautify((JSONObject) ele));
            }else if(ele instanceof JSONArray){
                sb.append(beautify((JSONArray) ele));
            }else if(ele instanceof String){
                sb.append(getIndentString());
                sb.append("\"");
                sb.append(ele);
                sb.append("\"");
            }else{
                sb.append(getIndentString());
                sb.append(ele);
            }

            if(i < size-1){
                sb.append(',');
            }
        }

        callDepth--;
        sb.append("\n");
        sb.append(getIndentString());
        sb.append("]");

        return sb.toString();
    }

    /**
     * 生成指定的缩进
     * @return
     */
    private static String getIndentString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < callDepth * INDENT_SIZE; i++) {
            sb.append(SPACE_CHAR);
        }
        return sb.toString();
    }
}
