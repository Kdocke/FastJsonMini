package com.kdocke.fastjson.parser;

/**
 * Json 解析时的配置
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/14 - 9:47
 */
public class ParserConfig {

    public static ParserConfig global = new ParserConfig();

    public final SymbolTable symbolTable = new SymbolTable(4096);

    /**
     * 返回一个 ParserConfig 实例
     * @return
     */
    public static ParserConfig getGlobalInstance() {
        return global;
    }

}
