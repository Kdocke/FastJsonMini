package com.kdocke.fastjson.parser;

/**
 * Json 词法分析器接口
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/17 - 8:39
 */
public interface JSONLexer {

    char EOI            = 0x1A;

    int  NOT_MATCH      = -1;
    int  NOT_MATCH_NAME = -2;
    int  UNKNOWN         = 0;
    int  OBJECT         = 1;
    int  ARRAY          = 2;
    int  VALUE          = 3;
    int  END            = 4;
    int  VALUE_NULL     = 5;

    int token();

    void nextToken();
    void nextToken(int expect);

    char getCurrent();
    char next();

    String scanSymbol(final SymbolTable symbolTable, final char quote);

    void resetStringPosition();

    void scanNumber();
    void scanString();

    boolean isBlankInput();

    int pos();

    Number integerValue();
    Number decimalValue(boolean decimal);
    String stringVal();

    void skipWhitespace();

    void close();

}
