package com.kdocke.test;

import com.kdocke.fastjson.JSON;
import com.kdocke.fastjson.JSONObject;
import com.kdocke.fastjson.util.BeautifyJsonUtils;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/26 - 10:37
 */
public class DeSerializerTest {

    public static void main(String[] args) {

        String json = "{\"name\":\"狄仁杰\",\"type\":\"射手\",\"ability\":[\"六令追凶\",\"逃脱\",\"王朝密令\"],\"history\":{\"DOB\":630,\"DOD\":700,\"position\":\"宰相\",\"dynasty\":\"唐朝\"}}";

        JSONObject parse = (JSONObject) JSON.parse(json);

        System.out.println(parse);

        System.out.println(BeautifyJsonUtils.beautify(parse));

    }

}
