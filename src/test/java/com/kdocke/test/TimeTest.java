package com.kdocke.test;

import com.kdocke.fastjson.JSON;
import com.kdocke.fastjson.JSONObject;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/23 - 15:35
 */
public class TimeTest {

    public static void main(String[] args) {

        String json = "{\"name\":\"狄仁杰\",\"type\":\"射手\",\"ability\":[\"六令追凶\",\"逃脱\",\"王朝密令\"],\"history\":{\"DOB\":630,\"DOD\":700,\"position\":\"宰相\",\"dynasty\":\"唐朝\"}}";

        long l1 = System.currentTimeMillis();
        JSONObject parse = (JSONObject) JSON.parse(json);
        long l2 = System.currentTimeMillis();

        System.out.print((l2 - l1) + " ");
    }

}
