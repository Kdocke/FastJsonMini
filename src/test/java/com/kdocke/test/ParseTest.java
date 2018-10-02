package com.kdocke.test;

import com.kdocke.fastjson.JSON;
import com.kdocke.fastjson.JSONArray;
import com.kdocke.fastjson.JSONObject;
import com.kdocke.fastjson.util.BeautifyJsonUtils;

import java.util.SortedMap;

/**
 * 问题：无法解析小数和布尔
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/19 - 17:36
 */
public class ParseTest {

    public static void main(String[] args) {
        String json = "{\"name\":\"狄仁杰\",\"type\":\"射手\",\"ability\":[\"六令追凶\",\"逃脱\",\"王朝密令\"],\"history\":{\"DOB\":630,\"DOD\":700,\"position\":\"宰相\",\"dynasty\":\"唐朝\"}}";

        System.out.println(json);

        JSONObject parse = (JSONObject) JSON.parse(json);

        System.out.println(parse);
//        System.out.println(BeautifyJsonUtils.beautify(parse));
        System.out.println(parse.get("name"));
        System.out.println(parse.get("type"));

        System.out.println("------");

        JSONObject history = (JSONObject) parse.get("history");
        System.out.println(history);
        System.out.println(history.get("DOD"));

        System.out.println("------");

        JSONArray ability = (JSONArray) parse.get("ability");
        System.out.println(ability);
        System.out.println(ability.get(0));
        System.out.println(ability.get(2));
    }
}
