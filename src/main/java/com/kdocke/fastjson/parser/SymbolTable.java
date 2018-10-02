package com.kdocke.fastjson.parser;

/**
 * 字符表
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/17 - 16:31
 */
public class SymbolTable {

    private final String[] symbols;
    private final int      indexMask;

    public SymbolTable(int tableSize){
        this.indexMask = tableSize - 1;
        this.symbols = new String[tableSize];
    }

    public String addSymbol(char[] buffer, int offset, int len) {
        // search for identical symbol
        int hash = hash(buffer, offset, len);
        return addSymbol(buffer, offset, len, hash);
    }

    public String addSymbol(char[] buffer, int offset, int len, int hash) {
        final int bucket = hash & indexMask;

        String symbol = symbols[bucket];
        if (symbol != null) {
            boolean eq = true;
            if (hash == symbol.hashCode() //
                    && len == symbol.length()) {
                for (int i = 0; i < len; i++) {
                    if (buffer[offset + i] != symbol.charAt(i)) {
                        eq = false;
                        break;
                    }
                }
            } else {
                eq = false;
            }

            if (eq) {
                return symbol;
            } else {
                return new String(buffer, offset, len);
            }
        }

        symbol = new String(buffer, offset, len).intern();
        symbols[bucket] = symbol;
        return symbol;
    }

    public String addSymbol(String buffer, int offset, int len, int hash) {
        return addSymbol(buffer, offset, len, hash, false);
    }

    /**
     * 添加字符symble
     * 实现原理：先用传入的 hash & indexMask 算出 bucket,
     * 若所添加的 symble 已存在，直接返回此 symble;
     * 否则添加 symble, 返回 symble.
     * @param buffer 传入的字符symble
     * @param offset 偏移
     * @param len 长度
     * @param hash symble 算出的hash
     * @param replace 是否替换
     * @return
     */
    public String addSymbol(String buffer, int offset, int len, int hash, boolean replace) {
        final int bucket = hash & indexMask;

        String symbol = symbols[bucket];
        if (symbol != null) {
            if (hash == symbol.hashCode() //
                    && len == symbol.length() //
                    && buffer.startsWith(symbol, offset)) {
                return symbol;
            }

            String str = subString(buffer, offset, len);

            if (replace) {
                symbols[bucket] = str;
            }

            return str;
        }

        symbol = len == buffer.length() //
                ? buffer //
                : subString(buffer, offset, len);
        symbol = symbol.intern();
        symbols[bucket] = symbol;
        return symbol;
    }

    /**
     * 对传入的字符串进行切割
     * @param src 传入的源字符串
     * @param offset 偏移
     * @param len 切割长度
     * @return 返回切割后的字符串
     */
    private static String subString(String src, int offset, int len) {
        char[] chars = new char[len];
        src.getChars(offset, offset + len, chars, 0);
        return new String(chars);
    }

    public static int hash(char[] buffer, int offset, int len) {
        int h = 0;
        int off = offset;

        for (int i = 0; i < len; i++) {
            h = 31 * h + buffer[off++];
        }
        return h;
    }
}
