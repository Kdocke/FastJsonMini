package com.kdocke.fastjson.parser;

import com.kdocke.fastjson.JSONArray;
import com.kdocke.fastjson.JSONException;
import com.kdocke.fastjson.JSONObject;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static com.kdocke.fastjson.parser.JSONLexer.EOI;
import static com.kdocke.fastjson.parser.JSONToken.*;

/**
 * 默认的 Json 解析器
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/16 - 16:45
 */
public class DefaultJSONParser implements Closeable {

    /**
     * 接收 json 串
     */
    public final Object input;
    /**
     * 字符表
     */
    public final SymbolTable symbolTable;
    protected ParserConfig config;

    public final JSONLexer lexer;

    protected ParseContext             context;

    private ParseContext[]             contextArray;
    /**
     * 上下文数组索引
     */
    private int contextArrayIndex  = 0;

    /**
     * @param input json串
     * @param config
     * @param features
     */
    public DefaultJSONParser(final String input, final ParserConfig config, int features){
        this(input, new JSONScanner(input, features), config);
    }

    /**
     * 构造时，根据首字符判断是 '{' 还是 '['
     * @param input json 串
     * @param lexer json 解析器
     * @param config
     */
    public DefaultJSONParser(final Object input, final JSONLexer lexer, final  ParserConfig config) {
        this.lexer = lexer;
        this.input = input;
        this.config = config;
        this.symbolTable = config.symbolTable;

        int ch = lexer.getCurrent();
        if (ch == '{'){
            lexer.next();
            ((JSONLexerBase) lexer).token = LBRACE;
        }else if (ch == '[') {
            lexer.next();
            ((JSONLexerBase) lexer).token = JSONToken.LBRACKET;
        }else {
            lexer.nextToken(); // prime the pump
        }
    }

    public Object parse(){
        return parse(null);
    }

    public Object parse(Object fieldName){
        final JSONLexer lexer = this.lexer;

        switch (lexer.token()){
            case SET:
                /** 探测到是 Set 集合类型，解析值 */
                lexer.nextToken();
                HashSet<Object> set = new HashSet<>();
                parseArray(set, fieldName);
                return set;
            case TREE_SET:
                /** 探测到是 TreeSet 集合类型，解析值 */
                lexer.nextToken();
                TreeSet<Object> treeSet = new TreeSet<Object>();
                parseArray(treeSet, fieldName);
                return treeSet;
            case LBRACKET:
                /** 探测到是数组集合类型，解析值 */
                JSONArray array = new JSONArray();
                parseArray(array, fieldName);
                return array;
            case LBRACE:
                /** 探测到是对象类型，解析值 */
                JSONObject object = new JSONObject(false);
                return parseObject(object, fieldName);
            case LITERAL_INT:
                /** 解析整数类型，预读下一个 token */
                Number intValue = lexer.integerValue();
                lexer.nextToken();
                return intValue;
            case LITERAL_FLOAT:
                /** 探测到是浮点类型，解析值 */
                Object value = lexer.decimalValue(false);
                lexer.nextToken();
                return value;
            case LITERAL_STRING:
                /** 探测到是字符串类型，解析值 */
                String stringLiteral = lexer.stringVal();
                lexer.nextToken(JSONToken.COMMA);

                return stringLiteral;
            case NULL:
                /** 探测到是 null，预读下一个 token */
                lexer.nextToken();
                return null;
            case UNDEFINED:
                /** 探测到是 undefined，预读下一个 token */
                lexer.nextToken();
                return null;
            case TRUE:
                /** 探测到是 true，预读下一个 token */
                lexer.nextToken();
                return Boolean.TRUE;
            case FALSE:
                /** 探测到是 false，预读下一个 token */
                lexer.nextToken();
                return Boolean.FALSE;
            case NEW:
                /** 期望是标识符，预读下一个 token */
                lexer.nextToken(JSONToken.IDENTIFIER);

                if (lexer.token() != JSONToken.IDENTIFIER) {
                    throw new JSONException("syntax error");
                }
                lexer.nextToken(JSONToken.LPAREN);

                long time = ((Number) lexer.integerValue()).longValue();
                return new Date(time);
            case EOF:
                if (lexer.isBlankInput()) {
                    return null;
                }
                throw new JSONException("unterminated json string, ");
            case IDENTIFIER:
                /** 读取标识符 */
                String identifier = lexer.stringVal();
                if ("NaN".equals(identifier)) {
                    lexer.nextToken();
                    return null;
                }
                throw new JSONException("syntax error, ");
            case ERROR:
            default:
                throw new JSONException("syntax error, ");
        }
    }

    /**
     * 解析 Array
     * @param array set 集合
     * @param fieldName
     */
    public final void parseArray(final Collection array, Object fieldName){
        final JSONLexer lexer = this.lexer;

        // 判断是否为 SET 或 TREE_SET
        if (lexer.token() == JSONToken.SET || lexer.token() == JSONToken.TREE_SET){
            lexer.nextToken();
        }

        // 进一步判断
        if (lexer.token() != JSONToken.LBRACKET){
            throw new JSONException("syntax error, expect [, actual " + JSONToken.name(lexer.token()) + ", pos "
                    + lexer.pos() + ", fieldName " + fieldName);
        }

        // 根据期望的 Token 类型读取下一个 Token
        lexer.nextToken(LITERAL_STRING);

        ParseContext context = this.context;
        this.setContext(array, fieldName);
        try {
            for (int i = 0;;++i){

                // 跳过 ','
                while (lexer.token() == JSONToken.COMMA){
                    lexer.nextToken();
                    continue;
                }

                Object value;
                switch (lexer.token()){
                    case LITERAL_INT:
                        value = lexer.integerValue();
                        lexer.nextToken(JSONToken.COMMA);
                        break;
                    case LITERAL_FLOAT:
                        value = lexer.decimalValue(false);
                        lexer.nextToken(JSONToken.COMMA);
                        break;
                    case LITERAL_STRING:
                        String stringLiteral = lexer.stringVal();
                        lexer.nextToken(JSONToken.COMMA);
                        value = stringLiteral;
                        break;
                    case TRUE:
                        value = Boolean.TRUE;
                        lexer.nextToken(JSONToken.COMMA);
                        break;
                    case FALSE:
                        value = Boolean.FALSE;
                        lexer.nextToken(JSONToken.COMMA);
                        break;
                    case LBRACE:
                        JSONObject object = new JSONObject(false);
                        value = parseObject(object, i);
                        break;
                    case LBRACKET:
                        Collection items = new JSONArray();
                        parseArray(items, i);
                        value = items;
                        break;
                    case NULL:
                        value = null;
                        lexer.nextToken(LITERAL_STRING);
                        break;
                    case UNDEFINED:
                        value = null;
                        lexer.nextToken(LITERAL_STRING);
                        break;
                    case RBRACKET:
                        lexer.nextToken(JSONToken.COMMA);
                        return;
                    case EOF:
                        throw new JSONException("unclosed jsonArray");
                    default:
                        value = parse();
                        break;
                }

                array.add(value);

                if (lexer.token() == JSONToken.COMMA) {
                    lexer.nextToken(LITERAL_STRING);
                    continue;
                }
            }
        }finally {
            this.setContext(context);
        }
    }

    /**
     * 解析 Object
     * @param object map
     * @param fieldName
     * @return
     */
    public final Object parseObject(final Map object, Object fieldName){
        final JSONLexer lexer = this.lexer;

        if (lexer.token() == JSONToken.NULL) {
            /** token 是 null 字符, 预读下一个 token */
            lexer.nextToken();
            return null;
        }

        if (lexer.token() == JSONToken.RBRACE) {
            /** token 是 '}' 字符, 预读下一个 token */
            lexer.nextToken();
            return object;
        }

        if (lexer.token() == LITERAL_STRING && lexer.stringVal().length() == 0) {
            /** token 是零长度字符串, 预读下一个 token */
            lexer.nextToken();
            return object;
        }

        if (lexer.token() != LBRACE && lexer.token() != JSONToken.COMMA) {
            throw new JSONException("syntax error");
        }

        ParseContext context = this.context;
        try {
            Map map = object instanceof JSONObject ? ((JSONObject) object).getInnerMap() : object;

            boolean setContextFlag = false;
            for (;;){
                /** 忽略前置空格 */
                lexer.skipWhitespace();

                // 获取当前字符
                char ch = lexer.getCurrent();

                boolean isObjectKey = false;
                Object key;
                if (ch == '"'){
                    /** 扫描到字段 key 名字 */
                    key = lexer.scanSymbol(symbolTable, '"');
                    lexer.skipWhitespace();
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("expect ':' at " + lexer.pos() + ", name " + key);
                    }
                }else if (ch == '}'){
                    lexer.next();
                    lexer.resetStringPosition();
                    lexer.nextToken();

                    return object;
                }else if (ch == EOI) {
                    throw new JSONException("syntax error");
                } else if (ch == ',') {
                    throw new JSONException("syntax error");
                }else if ((ch >= '0' && ch <= '9') || ch == '-') {
                    /** 重置 buffer 索引位置 */
                    lexer.resetStringPosition();
                    /** 扫描 map 的 key 为数字类型 */
                    lexer.scanNumber();
                    try {
                        if (lexer.token() == LITERAL_INT) {
                            key = lexer.integerValue();
                        } else {
                            key = lexer.decimalValue(true);
                        }
                    } catch (NumberFormatException e) {
                        throw new JSONException("parse number key error");
                    }
                    ch = lexer.getCurrent();
                    if (ch != ':') {
                        throw new JSONException("parse number key error");
                    }
                }else if (ch == '{' || ch == '['){
                    lexer.nextToken();
                    key = parse();
                    isObjectKey = true;
                }else {
                    key = null;
                }

                if (!isObjectKey) {
                    lexer.next();
                    lexer.skipWhitespace();
                }

                ch = lexer.getCurrent();

                lexer.resetStringPosition();

                if (!setContextFlag){
                    if (this.context != null && fieldName == this.context.fieldName && object == this.context.object) {
                        context = this.context;
                    } else {
                        ParseContext contextR = setContext(object, fieldName);
                        if (context == null) {
                            context = contextR;
                        }
                        setContextFlag = true;
                    }
                }

                if (object.getClass() == JSONObject.class) {
                    if (key == null) {
                        key = "null";
                    }
                }

                Object value;
                if (ch == '"') {
                    lexer.scanString();
                    String strValue = lexer.stringVal();
                    value = strValue;

                    map.put(key, value);
                } else if (ch >= '0' && ch <= '9' || ch == '-') {
                    /** 扫描数字 */
                    lexer.scanNumber();
                    if (lexer.token() == LITERAL_INT) {
                        value = lexer.integerValue();
                    } else {
                        value = lexer.decimalValue(false);
                    }

                    map.put(key, value);
                }else if (ch == '['){   // 减少嵌套，兼容android
                    lexer.nextToken();

                    JSONArray list = new JSONArray();

                    if (fieldName == null) {
                        this.setContext(context);
                    }

                    this.parseArray(list, key);
                    value = list;
                    map.put(key, value);

                    if (lexer.token() == JSONToken.RBRACE) {
                        lexer.nextToken();
                        return object;
                    } else if (lexer.token() == JSONToken.COMMA) {
                        continue;
                    } else {
                        throw new JSONException("syntax error");
                    }
                }else if (ch == '{') { // 减少嵌套，兼容android
                    lexer.nextToken();

                    final boolean parentIsArray = fieldName != null && fieldName.getClass() == Integer.class;

                    Map input = new JSONObject(false);
                    ParseContext ctxLocal = null;
                    if (!parentIsArray) {
                        ctxLocal = setContext(context, input, key);
                    }

                    Object obj = null;
                    boolean objParsed = false;

                    if (!objParsed) {
                        obj = this.parseObject(input, key);
                    }

                    if (ctxLocal != null && input != obj) {
                        ctxLocal.object = object;
                    }

                    map.put(key, obj);

                    if (parentIsArray) {
                        setContext(obj, key);
                    }

                    if (lexer.token() == JSONToken.RBRACE) {
                        lexer.nextToken();

                        setContext(context);
                        return object;
                    } else if (lexer.token() == JSONToken.COMMA) {
                        if (parentIsArray) {
                            this.popContext();
                        } else {
                            this.setContext(context);
                        }
                        continue;
                    } else {
                        throw new JSONException("syntax error, ");
                    }
                }else {
                    lexer.nextToken();
                    value = parse();

                    map.put(key, value);

                    if (lexer.token() == JSONToken.RBRACE) {
                        lexer.nextToken();
                        return object;
                    } else if (lexer.token() == JSONToken.COMMA) {
                        continue;
                    } else {
                        throw new JSONException("syntax error, position at " + lexer.pos() + ", name " + key);
                    }
                }

                lexer.skipWhitespace();
                ch = lexer.getCurrent();
                if (ch == ',') {
                    lexer.next();
                    continue;
                } else if (ch == '}') {
                    lexer.next();
                    lexer.resetStringPosition();
                    lexer.nextToken();

                    // this.setContext(object, fieldName);
                    this.setContext(value, key);

                    return object;
                } else {
                    throw new JSONException("syntax error, position at " + lexer.pos() + ", name " + key);
                }
            }
        }finally {
            this.setContext(context);
        }

    }

    public void setContext(ParseContext context) {
        this.context = context;
    }

    public ParseContext setContext(Object object, Object fieldName) {
        return setContext(this.context, object, fieldName);
    }

    public ParseContext setContext(ParseContext parent, Object object, Object fieldName) {
        this.context = new ParseContext(parent, object, fieldName);
        addContext(this.context);

        return this.context;
    }

    private void addContext(ParseContext context){
        int i = contextArrayIndex++;

        if (contextArray == null){
            contextArray = new ParseContext[8];
        }else if (i >= contextArray.length){
            int newLen = (contextArray.length * 3) / 2;
            ParseContext[] newArray = new ParseContext[newLen];
            System.arraycopy(contextArray, 0, newArray, 0, contextArray.length);
            contextArray = newArray;
        }
        contextArray[i] = context;
    }

    public void popContext() {
        this.context = this.context.parent;

        if (contextArrayIndex <= 0) {
            return;
        }

        contextArrayIndex--;
        contextArray[contextArrayIndex] = null;
    }

    @Override
    public void close() {
        final JSONLexer lexer = this.lexer;

        try {
            if(lexer.token() != JSONToken.EOF){
                throw new JSONException("not close json text, token: " + JSONToken.name(lexer.token()));
            }
        }finally {
            lexer.close();
        }
    }
}
