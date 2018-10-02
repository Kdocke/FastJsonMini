package com.kdocke.fastjson.parser;

import com.kdocke.fastjson.JSON;

import java.math.BigDecimal;

/**
 * Json 扫描器
 * 这个类，为了性能优化做了很多特别处理，一切都是为了性能！！！
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/17 - 10:03
 */
public class JSONScanner extends JSONLexerBase {

    /**
     * 用于接收 json 串
     */
    private final String text;
    /**
     * 定义 json 串长度
     */
    private final int    len;

    public JSONScanner(String input){
        this(input, JSON.DEFAULT_PARSER_FEATURE);
    }

    public JSONScanner(String input, int features) {
        super(features);

        text = input;
        len = text.length();
        bp = -1;

        next();
        /**
         * 65279: UTF-8 的 BOM 标识和非法字符\65279
         */
        if (ch == 65279){
            next();
        }

    }

    /**
     * 读取下一个字符
     * @return
     */
    public final char next() {
        /** 递增 buffer 的位置 */
        int index = ++bp;

        /** 如果已经超过字符串长度，返回 EOI，否则去读一个字符 */
        return ch = (index >= this.len) ? EOI : text.charAt(index);
    }

    protected final void arrayCopy(int srcPos, char[] dest, int destPos, int length) {
        text.getChars(srcPos, srcPos + length, dest, destPos);
    }

    protected final void copyTo(int offset, int count, char[] dest) {
        text.getChars(offset, offset + count, dest, 0);
    }

    public final String stringVal() {
        if (!hasSpecial) {
            return this.subString(np + 1, sp);
        } else {
            return new String(sbuf, 0, sp);
        }
    }

    public final BigDecimal decimalValue() {
        char chLocal = charAt(np + sp - 1);

        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            sp--;
        }

        int offset = np, count = sp;
        if (count < sbuf.length) {
            text.getChars(offset, offset + count, sbuf, 0);
            return new BigDecimal(sbuf, 0, count);
        } else {
            char[] chars = new char[count];
            text.getChars(offset, offset + count, chars, 0);
            return new BigDecimal(chars);
        }
    }

    public final String subString(int offset, int count) {
            return text.substring(offset, offset + count);
    }

    public final char charAt(int index) {
        if (index >= len) {
            return EOI;
        }

        return text.charAt(index);
    }

    public final String numberString() {
        /** 取 token 最后一个字符 */
        char chLocal = charAt(np + sp - 1);

        int sp = this.sp;
        if (chLocal == 'L' || chLocal == 'S' || chLocal == 'B' || chLocal == 'F' || chLocal == 'D') {
            sp--;
        }

        /** 取数字类型字符串，不包括后缀类型 */
        return this.subString(np, sp);
    }

    public final String addSymbol(int offset, int len, int hash, final SymbolTable symbolTable) {
        return symbolTable.addSymbol(text, offset, len, hash);
    }

    @Override
    public boolean isEOF() {
        /** 如果到达了流长度、或者遇到 EOI 结束符 认为结束 */
        return bp == len || ch == EOI && bp + 1 == len;
    }


}
