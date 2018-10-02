package com.kdocke.fastjson;

/**
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/18 - 9:04
 */
public class JSONException extends RuntimeException {

    public JSONException(){
        super();
    }

    public JSONException(String message){
        super(message);
    }

    public JSONException(String message, Throwable cause){
        super(message, cause);
    }

}
