package com.kdocke.fastjson.parser;

import com.kdocke.fastjson.JSONException;

import java.io.Closeable;
import java.math.BigDecimal;
import java.math.BigInteger;

import static com.kdocke.fastjson.parser.JSONToken.*;

/**
 * Json 词法分析器基类
 * JSONLexerBase定义并实现了json串实现解析机制的基础
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/17 - 8:49
 */
public abstract class JSONLexerBase implements JSONLexer, Closeable {

    /** 当前 token 含义 */
    protected int                            token;
    /** 记录当前扫描字符的位置 */
    protected int                            pos;
    protected int                            features;

    /** 当前有效字符 */
    protected char                           ch;
    /** 流(或者json字符串)中当前的位置, 每次读取字符会自增 */
    protected int                            bp;

    /** 结束位置 */
    protected int                            eofPos;

    /**
     * number start position <br>
     * 可以理解为 找到 token 时, token 的首字符位置,
     * 和 bp 不一样, 这个不会递增, 会在开始 token 前记录一次
     */
    protected int                            np;

    /**
     * 是否有特殊字符
     */
    protected boolean                        hasSpecial;

    /** 字符缓冲区 */
    protected char[]                         sbuf;
    /** 字符缓冲区的索引,指向下一个可写字符的位置，也代表字符缓冲区字符数量 */
    protected int                            sp;

    protected static final long  MULTMIN_RADIX_TEN     = Long.MIN_VALUE / 10;

    /**
     * 匹配状态
     */
    public int                               matchStat          = UNKNOWN;

    /**
     * 线程局部变量:
     *      保证变量是同一个，但是每个线程都使用同一个初始值，也就是使用同一个变量的一个新的副本。
     */
    private final static ThreadLocal<char[]> SBUF_LOCAL = new ThreadLocal<>();

    public abstract char next();

    /**
     * 数字数组: 长度: 103
     */
    protected final static int[] digits = new int[(int)'f' + 1];

    public JSONLexerBase(int features){
        this.features = features;

        sbuf = SBUF_LOCAL.get();

        if(sbuf == null){
            sbuf = new char[512];
        }
    }

    static {
        for (int i = '0'; i <= '9'; i++) {
            digits[i] = i - '0';
        }

        for (int i = 'a'; i <= 'f'; ++i) {
            digits[i] = (i - 'a') + 10;
        }

        for (int i = 'A'; i <= 'F'; ++i) {
            digits[i] = (i - 'A') + 10;
        }
    }

    public final char getCurrent() {
        return ch;
    }

    /**
     * 读取下一个 Token
     */
    public final void nextToken(){
        // 将字符 buffer pos 设置为初始0
        sp = 0;

        for (;;){
            // pos 记录为流的当前位置
            pos = bp;

            // 读取引号内的字符串
            if (ch == '"'){
                scanString();
                return;
            }

            // 处理 ','
            if (ch == ',') {
                /** 跳过当前，读取下一个字符 */
                next();
                token = COMMA;
                return;
            }

            // 处理 整数
            if(ch >= '0' && ch <= '9'){
                /** 读取整数 */
                scanNumber();
                return;
            }

            if (ch == '-') {
                /** 读取负数 */
                scanNumber();
                return;
            }

            switch (ch){
                case ' ':
                case '\t':
                case '\b':
                case '\f':
                case '\n':
                case '\r':
                    // 处理空字符
                    next();
                    break;
                case 't':   // true
                    /** 读取字符 true */
                    scanTrue();
                    return;
                case 'f': // false
                    /** 读取字符 false */
                    scanFalse();
                    return;
                case 'n': // new,null
                    /** 读取为new或者null的token */
                    scanNullOrNew();
                    return;
                case 'T':
                case 'N': // NULL
                case 'S':
                case 'u': // undefined
                    /** 读取标识符，已经自动预读了下一个字符 */
                    scanIdent();
                    return;
                case '(':
                    next();
                    token = LPAREN;
                    return;
                case ')':
                    next();
                    token = RPAREN;
                    return;
                case '[':
                    next();
                    token = LBRACKET;
                    return;
                case ']':
                    next();
                    token = RBRACKET;
                    return;
                case '{':
                    next();
                    token = LBRACE;
                    return;
                case '}':
                    next();
                    token = RBRACE;
                    return;
                case ':':
                    next();
                    token = COLON;
                    return;
                case ';':
                    next();
                    token = SEMI;
                    return;
                case '.':
                    next();
                    token = DOT;
                    return;
                case '+':
                    next();
                    scanNumber();
                    return;
                case 'x':
                    scanHex();
                    return;
                default:
                    if (isEOF()) { // JLS
                        if (token == EOF) {
                            throw new JSONException("EOF error");
                        }

                        token = EOF;
                        pos = bp = eofPos;
                    } else {
                        /** 忽略控制字符或者删除字符 */
                        if (ch <= 31 || ch == 127) {
                            next();
                            break;
                        }
                        next();
                    }
                    return;
            }
        }
    }

    /**
     * 根据期望字符扫描 token
     */
    public final void nextToken(int expect){
        /** 将字符buffer pos设置为初始0 */
        sp = 0;

        for (;;){
            switch (expect){
                case JSONToken.LBRACE:
                    if (ch == '{') {
                        token = JSONToken.LBRACE;
                        next();
                        return;
                    }
                    if (ch == '[') {
                        token = JSONToken.LBRACKET;
                        next();
                        return;
                    }
                    break;
                case JSONToken.COMMA:
                    if (ch == ',') {
                        token = JSONToken.COMMA;
                        next();
                        return;
                    }

                    if (ch == '}') {
                        token = RBRACE;
                        next();
                        return;
                    }

                    if (ch == ']') {
                        token = JSONToken.RBRACKET;
                        next();
                        return;
                    }

                    if (ch == EOI) {
                        token = JSONToken.EOF;
                        return;
                    }
                    break;
                case JSONToken.LITERAL_INT:
                    if (ch >= '0' && ch <= '9') {
                        pos = bp;
                        scanNumber();
                        return;
                    }

                    if (ch == '"') {
                        pos = bp;
                        scanString();
                        return;
                    }

                    if (ch == '[') {
                        token = JSONToken.LBRACKET;
                        next();
                        return;
                    }

                    if (ch == '{') {
                        token = JSONToken.LBRACE;
                        next();
                        return;
                    }
                    break;
                case LITERAL_STRING:
                    if (ch == '"') {
                        pos = bp;
                        scanString();
                        return;
                    }

                    if (ch >= '0' && ch <= '9') {
                        pos = bp;
                        scanNumber();
                        return;
                    }

                    if (ch == '[') {
                        token = JSONToken.LBRACKET;
                        next();
                        return;
                    }

                    if (ch == '{') {
                        token = JSONToken.LBRACE;
                        next();
                        return;
                    }
                    break;
                case JSONToken.LBRACKET:
                    if (ch == '[') {
                        token = JSONToken.LBRACKET;
                        next();
                        return;
                    }

                    if (ch == '{') {
                        token = JSONToken.LBRACE;
                        next();
                        return;
                    }
                    break;
                case JSONToken.RBRACKET:
                    if (ch == ']') {
                        token = JSONToken.RBRACKET;
                        next();
                        return;
                    }
                case JSONToken.EOF:
                    if (ch == EOI) {
                        token = JSONToken.EOF;
                        return;
                    }
                    break;
                case JSONToken.IDENTIFIER:
                    return;
                default:
                    break;
            }
        }

    }

    /**
     * 返回 当前 Token 类型
     * @return int
     */
    public final int token(){
        return token;
    }

    /**
     * 返回 当前扫描到的字符位置
     * @return
     */
    public final int pos(){
        return pos;
    }

    /**
     * 扫描 字符串
     */
    public final void scanString(){
        // 记录当前流中 token 的开始位置
        np = bp;
        hasSpecial = false;
        char ch;
        for(;;) {

            // 读取当前字符串的字符
            ch = next();

            // 如果遇到字符串结束符 '"', 则结束字符串读取
            if (ch == '\"') {
                break;
            }

            // 如果遇到结束符EOI, 但是没有遇到流的结尾, 添加 EOI 结束符
            if (ch == EOI) {
                if (!isEOF()) {
                    putChar((char) EOI);
                    continue;
                }
                throw new JSONException("unclosed string : " + ch);
            }

            // 处理转义字符逻辑
            if (ch == '\\') {
                if (!hasSpecial) {
                    hasSpecial = true;      // 第一次遇到认为 '\' 是特殊符号

                    // 如果 buffer 空间不够, 执行 2 倍扩容
                    if (sp >= sbuf.length) {
                        int newCapcity = sbuf.length * 2;
                        if (sp > newCapcity) {
                            newCapcity = sp;
                        }
                        char[] newsbuf = new char[newCapcity];

                        /**
                         * 将原 sbuf 数组中的内容复制到新的扩容后的 newsbuf 新数组中
                         * 并将新数组赋值给原 sbuf
                         */
                        System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
                        sbuf = newsbuf;
                    }
                    // 复制有效字符串到 buffer 中, 不包括引号
                    copyTo(np + 1, sp, sbuf);
                }

                // 读取转义字符 '\' 下一个字符
                ch = next();

                // 转换 ascii 字符
                switch (ch) {
                    case '0':
                        /** 空字符 */
                        putChar('\0');
                        break;
                    case '1':
                        /** 标题开始 */
                        putChar('\1');
                        break;
                    case '2':
                        /** 正文开始 */
                        putChar('\2');
                        break;
                    case '3':
                        /** 正文结束 */
                        putChar('\3');
                        break;
                    case '4':
                        /** 传输结束 */
                        putChar('\4');
                        break;
                    case '5':
                        /** 请求 */
                        putChar('\5');
                        break;
                    case '6':
                        /** 收到通知 */
                        putChar('\6');
                        break;
                    case '7':
                        /** 响铃 */
                        putChar('\7');
                        break;
                    case 'b': // 8
                        /** 退格 */
                        putChar('\b');
                        break;
                    case 't': // 9
                        /** 水平制表符 */
                        putChar('\t');
                        break;
                    case 'n': // 10
                        /** 换行键 */
                        putChar('\n');
                        break;
                    case 'v': // 11
                        /** 垂直制表符 */
                        putChar('\u000B');
                        break;
                    case 'f': // 12
                        /** 换页键 */
                    case 'F':
                        /** 换页键 */
                        putChar('\f');
                        break;
                    case 'r': // 13
                        /** 回车键 */
                        putChar('\r');
                        break;
                    case '"': // 34
                        /** 双引号 */
                        putChar('"');
                        break;
                    case '\'': // 39
                        /** 闭单引号 */
                        putChar('\'');
                        break;
                    case '/': // 47
                        /** 斜杠 */
                        putChar('/');
                        break;
                    case '\\': // 92
                        /** 反斜杠 */
                        putChar('\\');
                        break;
                    case 'x':
                        /** 小写字母x, 标识一个字符 */
                        char x1 = ch = next();
                        char x2 = ch = next();

                        int x_val = digits[x1] * 16 + digits[x2];
                        char x_char = (char) x_val;
                        putChar(x_char);
                        break;
                    case 'u':
                        /** 小写字母u, 标识一个字符 */
                        char u1 = ch = next();
                        char u2 = ch = next();
                        char u3 = ch = next();
                        char u4 = ch = next();
                        int val = Integer.parseInt(new String(new char[]{u1, u2, u3, u4}), 16);
                        putChar((char) val);
                        break;
                    default:
                        this.ch = ch;
                        throw new JSONException("unclosed string : " + ch);
                }
                continue;
            }

            /** 没有转译字符，递增buffer字符位置 */
            if (!hasSpecial) {
                sp++;
                continue;
            }

            /** 继续读取转译字符后面的字符 */
            if (sp == sbuf.length) {
                putChar(ch);
            } else {
                sbuf[sp++] = ch;
            }

        }

        token = LITERAL_STRING;
        /** 自动预读下一个字符 */
        this.ch = next();
    }

    /**
     * 扫描 整数
     */
    public final void scanNumber(){
        /** 记录当前流中token的开始位置, np指向数字字符索引 */
        np = bp;

        /** 兼容处理负数 */
        if (ch == '-') {
            sp++;
            next();
        }

        for (;;){
            if (ch >= '0' && ch <= '9'){
                /** 如果是数字字符，递增索引位置 */
                sp++;
            }else {
                break;
            }
            next();
        }

        boolean isDouble = false;

        /** 如果遇到小数点字符 */
        if (ch == '.'){
            sp++;
            /** 继续读小数点后面字符 */
            next();
            isDouble = true;

            for (;;) {
                if (ch >= '0' && ch <= '9') {
                    sp++;
                } else {
                    break;
                }
                next();
            }
        }

        /** 继续读取数字后面的类型 */
        if (ch == 'L') {
            sp++;
            next();
        } else if (ch == 'S') {
            sp++;
            next();
        } else if (ch == 'B') {
            sp++;
            next();
        } else if (ch == 'F') {
            sp++;
            next();
            isDouble = true;
        } else if (ch == 'D') {
            sp++;
            next();
            isDouble = true;
        } else if (ch == 'e' || ch == 'E') {

            /** 扫描科学计数法 */
            sp++;
            next();

            if (ch == '+' || ch == '-') {
                sp++;
                next();
            }

            for (;;) {
                if (ch >= '0' && ch <= '9') {
                    sp++;
                } else {
                    break;
                }
                next();
            }

            if (ch == 'D' || ch == 'F') {
                sp++;
                next();
            }

            isDouble = true;
        }

        if (isDouble){
            token = JSONToken.LITERAL_FLOAT;
        }else {
            token = JSONToken.LITERAL_INT;
        }
    }

    /**
     * 扫描 Boolean: true
     */
    public final void scanTrue(){
        if (ch != 't') {
            throw new JSONException("error parse true");
        }
        next();

        if (ch != 'r') {
            throw new JSONException("error parse true");
        }
        next();

        if (ch != 'u') {
            throw new JSONException("error parse true");
        }
        next();

        if (ch != 'e') {
            throw new JSONException("error parse true");
        }
        next();

        if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI
                || ch == '\f' || ch == '\b' || ch == ':' || ch == '/') {
            /** 兼容性防御，标记是true的token */
            token = JSONToken.TRUE;
        }else {
            throw new JSONException("scan true error");
        }

    }

    /**
     * 扫描 Boolean: false
     */
    public final void scanFalse() {
        if (ch != 'f') {
            throw new JSONException("error parse false");
        }
        next();

        if (ch != 'a') {
            throw new JSONException("error parse false");
        }
        next();

        if (ch != 'l') {
            throw new JSONException("error parse false");
        }
        next();

        if (ch != 's') {
            throw new JSONException("error parse false");
        }
        next();

        if (ch != 'e') {
            throw new JSONException("error parse false");
        }
        next();

        if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI
                || ch == '\f' || ch == '\b' || ch == ':' || ch == '/') {
            token = JSONToken.FALSE;
        } else {
            throw new JSONException("scan false error");
        }
    }

    /**
     * 扫描 null 或 new
     */
    public final void scanNullOrNew() {
        if (ch != 'n') {
            throw new JSONException("error parse null or new");
        }
        next();

        if (ch == 'u') {
            next();
            if (ch != 'l') {
                throw new JSONException("error parse null");
            }
            next();

            if (ch != 'l') {
                throw new JSONException("error parse null");
            }
            next();

            if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI
                    || ch == '\f' || ch == '\b') {
                token = JSONToken.NULL;
            } else {
                throw new JSONException("scan null error");
            }
            return;
        }

        if (ch != 'e') {
            throw new JSONException("error parse new");
        }
        next();

        if (ch != 'w') {
            throw new JSONException("error parse new");
        }
        next();

        if (ch == ' ' || ch == ',' || ch == '}' || ch == ']' || ch == '\n' || ch == '\r' || ch == '\t' || ch == EOI
                || ch == '\f' || ch == '\b') {
            token = JSONToken.NEW;
        } else {
            throw new JSONException("scan new error");
        }
    }

    /**
     * 扫描标识符
     */
    public final void scanIdent(){
        /** 记录当前流中token的开始位置, np指向当前token前一个字符 */
        np = bp - 1;
        hasSpecial = false;

        for (;;){
            sp++;

            next();
            /** 如果是字母或数字，继续读取 */
            if (Character.isLetterOrDigit(ch)) {
                continue;
            }

            /** 获取字符串值 */
            String ident = stringVal();

            if ("null".equalsIgnoreCase(ident)) {
                token = JSONToken.NULL;
            } else if ("new".equals(ident)) {
                token = JSONToken.NEW;
            } else if ("true".equals(ident)) {
                token = JSONToken.TRUE;
            } else if ("false".equals(ident)) {
                token = JSONToken.FALSE;
            } else if ("undefined".equals(ident)) {
                token = JSONToken.UNDEFINED;
            } else if ("Set".equals(ident)) {
                token = JSONToken.SET;
            } else if ("TreeSet".equals(ident)) {
                token = JSONToken.TREE_SET;
            } else {
                token = JSONToken.IDENTIFIER;
            }
            return;
        }
    }

    /**
     * 扫描十六进制数
     */
    public final void scanHex(){
        if (ch != 'x') {
            throw new JSONException("illegal state. " + ch);
        }
        next();

        /** 十六进制x紧跟着单引号 */
        if (ch != '\'') {
            throw new JSONException("illegal state. " + ch);
        }

        np = bp;
        /** 这里一次next, for循环也读一次next, 因为十六进制被写成2个字节的单字符 */
        next();

        for (int i = 0;;++i) {
            char ch = next();
            if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F')) {
                sp++;
                continue;
            } else if (ch == '\'') {
                sp++;
                /** 遇到结束符号，自动预读下一个字符 */
                next();
                break;
            } else {
                throw new JSONException("illegal state. " + ch);
            }
        }
        token = JSONToken.HEX;
    }

    /**
     * Int 类型字段解析：当反序列化java对象遇到整型int.class字段会调用该方法解析    <br>
     *
     * scanInt(char)方法考虑了数字加引号的情况，当遇到下列情况认为匹配失败：
     * 1.扫描遇到的数字遇到标点符号
     * 2.扫描的数字范围溢出
     * 3.扫描到的非数字并且不是 null
     * 4.忽略空白字符的情况下，读取数字后结束符和期望 expectNext 不一致
     * @param expectNext
     * @return
     */
    public int scanInt(char expectNext){
        matchStat = UNKNOWN;

        int offset = 0;
        char chLocal = charAt(bp + (offset++));

        /** 取整数第一个字符判断是否是引号 */
        final boolean quote = chLocal == '"';
        if (quote) {
            /** 如果是双引号，取第一个数字字符 */
            chLocal = charAt(bp + (offset++));
        }

        final boolean negative = chLocal == '-';
        if (negative) {
            /** 如果是负数，继续取下一个字符 */
            chLocal = charAt(bp + (offset++));
        }

        int value;
        /** 是数字类型 */
        if (chLocal >= '0' && chLocal <= '9') {
            value = chLocal - '0';
            for (;;){
                /** 循环将字符转换成数字 */
                chLocal = charAt(bp + (offset++));
                if (chLocal >= '0' && chLocal <= '9') {
                    value = value * 10 + (chLocal - '0');
                } else if (chLocal == '.') {
                    matchStat = NOT_MATCH;
                    return 0;
                } else {
                    break;
                }
            }
            if (value < 0) {
                matchStat = NOT_MATCH;
                return 0;
            }
        }else {
            matchStat = NOT_MATCH;
            return 0;
        }

        for (;;) {
            if (chLocal == expectNext) {
                bp += offset;
                this.ch = this.charAt(bp);
                matchStat = VALUE;
                token = JSONToken.COMMA;
                return negative ? -value : value;
            } else {
                if (isWhitespace(chLocal)) {
                    chLocal = charAt(bp + (offset++));
                    continue;
                }
                matchStat = NOT_MATCH;
                return negative ? -value : value;
            }
        }
    }

    /**
     * 根据 token 识别数字
     * @return
     */
    public final Number integerValue(){
        long result = 0;    // 接收最后结果
        boolean negative = false;   // 是否为负数
        if (np == -1){
            np = 0;
        }

        /** np是token开始索引, sp是buffer索引，也代表buffer字符个数 */
        int i = np, max = np + sp;
        long limit;     // 范围约束
        long multmin;
        int digit;

        char type = ' ';

        /** 探测数字类型最后一位是否带类型 */
        switch (charAt(max - 1)) {
            case 'L':
                max--;
                type = 'L';
                break;
            case 'S':
                max--;
                type = 'S';
                break;
            case 'B':
                max--;
                type = 'B';
                break;
            default:
                break;
        }

        /** 探测数字首字符是否是符号 */
        if (charAt(np) == '-') {
            negative = true;
            limit = Long.MIN_VALUE;
            i++;
        } else {
            limit = -Long.MAX_VALUE;
        }

        multmin = MULTMIN_RADIX_TEN;
        if (i < max){
            /** 数字第一个字母转换成数字 */
            digit = charAt(i++) - '0';
            result = -digit;
        }

        /** 快速处理高精度整数，因为整数最大是10^9次方 */
        while (i < max) {
            // Accumulating negatively avoids surprises near MAX_VALUE
            digit = charAt(i++) - '0';
            /** multmin 大概10^17 */
            if (result < multmin) {
                /** numberString获取到的不包含数字后缀类型，但是包括负数符号(如果有) */
                return new BigInteger(numberString());
            }
            result *= 10;
            if (result < limit + digit) {
                return new BigInteger(numberString());
            }
            result -= digit;
        }

        if (negative) {
            /** 处理完数字 i 是指向数字最后一个字符的下一个字符,
             *  这里判断 i > np + 1 , 代表在 有效数字字符范围
             */
            if (i > np + 1) {
                /** 这里根据类型具体后缀类型做一次转换 */
                if (result >= Integer.MIN_VALUE && type != 'L') {
                    if (type == 'S') {
                        return (short) result;
                    }

                    if (type == 'B') {
                        return (byte) result;
                    }

                    return (int) result;
                }
                return result;
            } else { /* Only got "-" */
                throw new NumberFormatException(numberString());
            }
        } else {
            /** 这里是整数， 因为前面处理成负数，取反就可以了 */
            result = -result;
            /** 这里根据类型具体后缀类型做一次转换 */
            if (result <= Integer.MAX_VALUE && type != 'L') {
                if (type == 'S') {
                    return (short) result;
                }

                if (type == 'B') {
                    return (byte) result;
                }

                return (int) result;
            }
            return result;
        }
    }

    /**
     * 小数解析
     * @param decimal
     * @return
     */
    public final Number decimalValue(boolean decimal) {
        /** 判断最后一个字符 */
        char chLocal = charAt(np + sp - 1);
        try {
            if (chLocal == 'F') {
                return Float.parseFloat(numberString());
            }

            if (chLocal == 'D') {
                return Double.parseDouble(numberString());
            }

            if (decimal) {
                return decimalValue();
            } else {
                return doubleValue();
            }
        } catch (NumberFormatException ex) {
            throw new JSONException(ex.getMessage());
        }
    }

    public abstract BigDecimal decimalValue();

    /**
     * Append a character to sbuf：向 sbuf 数组中添加一个字符
     * @param ch
     */
    protected final void putChar(char ch){
        if (sp == sbuf.length){
            char[] newsbuf = new char[sbuf.length *2];
            System.arraycopy(sbuf, 0, newsbuf, 0, sbuf.length);
            sbuf = newsbuf;
        }
        sbuf[sp++] = ch;
    }

    public final void resetStringPosition() {
        this.sp = 0;
    }

    public final String scanSymbol(final SymbolTable symbolTable, final char quote) {
        int hash = 0;

        np = bp;
        sp = 0;
        boolean hasSpecial = false;
        char chLocal;

        for (;;){
            // 将 chLocal 置为下一个字符
            chLocal = next();

            if (chLocal == quote) {
                break;
            }

            if (chLocal == EOI) {
                throw new JSONException("unclosed.str");
            }

            if (chLocal == '\\') {
                if (!hasSpecial) {
                    hasSpecial = true;
                    arrayCopy(np + 1, sbuf, 0, sp);
                }

                chLocal = next();

                switch (chLocal){
                    case '0':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\0');
                        break;
                    case '1':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\1');
                        break;
                    case '2':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\2');
                        break;
                    case '3':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\3');
                        break;
                    case '4':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\4');
                        break;
                    case '5':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\5');
                        break;
                    case '6':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\6');
                        break;
                    case '7':
                        hash = 31 * hash + (int) chLocal;
                        putChar('\7');
                        break;
                    case 'b': // 8
                        hash = 31 * hash + (int) '\b';
                        putChar('\b');
                        break;
                    case 't': // 9
                        hash = 31 * hash + (int) '\t';
                        putChar('\t');
                        break;
                    case 'n': // 10
                        hash = 31 * hash + (int) '\n';
                        putChar('\n');
                        break;
                    case 'v': // 11
                        hash = 31 * hash + (int) '\u000B';
                        putChar('\u000B');
                        break;
                    case 'f': // 12
                    case 'F':
                        hash = 31 * hash + (int) '\f';
                        putChar('\f');
                        break;
                    case 'r': // 13
                        hash = 31 * hash + (int) '\r';
                        putChar('\r');
                        break;
                    case '"': // 34
                        hash = 31 * hash + (int) '"';
                        putChar('"');
                        break;
                    case '\'': // 39
                        hash = 31 * hash + (int) '\'';
                        putChar('\'');
                        break;
                    case '/': // 47
                        hash = 31 * hash + (int) '/';
                        putChar('/');
                        break;
                    case '\\': // 92
                        hash = 31 * hash + (int) '\\';
                        putChar('\\');
                        break;
                    case 'x':
                        char x1 = ch = next();
                        char x2 = ch = next();

                        int x_val = digits[x1] * 16 + digits[x2];
                        char x_char = (char) x_val;
                        hash = 31 * hash + (int) x_char;
                        putChar(x_char);
                        break;
                    case 'u':
                        char c1 = chLocal = next();
                        char c2 = chLocal = next();
                        char c3 = chLocal = next();
                        char c4 = chLocal = next();
                        int val = Integer.parseInt(new String(new char[] { c1, c2, c3, c4 }), 16);
                        hash = 31 * hash + val;
                        putChar((char) val);
                        break;
                    default:
                        this.ch = chLocal;
                        throw new JSONException("unclosed.str.lit");
                }
                continue;
            }
            hash = 31 * hash + chLocal;

            if (!hasSpecial) {
                sp++;
                continue;
            }

            if (sp == sbuf.length) {
                putChar(chLocal);
            } else {
                sbuf[sp++] = chLocal;
            }
        }

        token = LITERAL_STRING;

        String value;
        if (!hasSpecial) {
            // return this.text.substring(np + 1, np + 1 + sp).intern();
            int offset;
            if (np == -1) {
                offset = 0;
            } else {
                offset = np + 1;
            }
            value = addSymbol(offset, sp, hash, symbolTable);
        } else {
            value = symbolTable.addSymbol(sbuf, 0, sp, hash);
        }

        sp = 0;
        this.next();

        return value;
    }

    public abstract String addSymbol(int offset, int len, int hash, final SymbolTable symbolTable);

    /**
     * 判断是否为空格
     * @param ch
     * @return
     */
    public static boolean isWhitespace(char ch) {
        // 专门调整了判断顺序
        return ch <= ' ' && (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\b');
    }

    /**
     * 判断是否为空白输入
     * @return
     */
    public boolean isBlankInput() {
        for (int i = 0;; ++i) {
            char chLocal = charAt(i);
            if (chLocal == EOI) {
                token = JSONToken.EOF;
                break;
            }

            if (!isWhitespace(chLocal)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将 Double 类型的字符串转成 Double
     * @return
     */
    public double doubleValue() {
        return Double.parseDouble(numberString());
    }

    public final void skipWhitespace() {
        for (;;) {
            if (ch <= '/') {
                if (ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t' || ch == '\f' || ch == '\b') {
                    next();
                    continue;
                } else if (ch == '/') {
                    continue;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    protected abstract void arrayCopy(int srcPos, char[] dest, int destPos, int length);
    protected abstract void copyTo(int offset, int count, char[] dest);

    public abstract String stringVal();
    public abstract String subString(int offset, int count);

    public abstract char charAt(int index);

    public abstract String numberString();

    public abstract boolean isEOF();

    @Override
    public void close() {
        if (sbuf.length <= 1024 * 8){
            SBUF_LOCAL.set(sbuf);
        }
        this.sbuf = null;
    }
}
