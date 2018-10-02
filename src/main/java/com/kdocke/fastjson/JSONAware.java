package com.kdocke.fastjson;

/**
 * 实现此接口可以支持自定义的 JSON 文本输出
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 9:06
 */
public interface JSONAware {

    /**
     * 返回 JSON 文本
     * @return JSON text
     */
    String toJSONString();

}
