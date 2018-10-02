package com.kdocke.fastjson.serializer;

import com.kdocke.fastjson.JSON;
import com.kdocke.fastjson.JSONException;
import sun.misc.IOUtils;
import sun.security.util.Length;

import java.io.IOException;
import java.io.Writer;

/**
 * 序列化输出器：用于缓存序列化结果字符
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/20 - 9:09
 */
public class SerializeWriter extends Writer {

    /** 字符类型 buffer */
    private final static ThreadLocal<char[]> bufLocal      = new ThreadLocal<>();

    /** 存储序列化结果 buffer */
    protected char                           buf[];

    /** buffer 中包含的字符数 */
    protected int                            count;

    /** 序列化的特性: 比如写枚举是按照名字还是枚举值 */
    protected int features;

    /** 序列化输出器 */
    private final Writer                     writer;

    public SerializeWriter(){
        this((Writer) null);
    }

    public SerializeWriter(Writer writer){
        this(writer, JSON.DEFAULT_GENERATE_FEATURE, SerializerFeature.EMPTY);
    }

    public SerializeWriter(Writer writer, int defaultFeatures, SerializerFeature... features) {
        this.writer = writer;

        // 初始化字符 buffer
        buf = bufLocal.get();
        if (buf != null){
            bufLocal.set(null);
        }else {
            buf = new char[2048];
        }

        this.features = defaultFeatures;
    }

    public SerializeWriter append(CharSequence csq){
        String s = (csq == null) ? "null" : csq.toString();
        write(s, 0, s.length());
        return this;
    }

    /**
     * 序列化单字符
     * 基本原理：直接赋值到 count 位, count+1
     * @param c
     */
    public void write(int c){
        buf[count] = (char) c;
        count = count + 1;
    }

    /**
     * 序列化字符数组
     * @param c
     * @param off
     * @param len
     */
    public void write(char[] c, int off, int len) {
        if (off < 0 //
                || off > c.length //
                || len < 0 //
                || off + len > c.length //
                || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        int newcount = count + len;
        if (newcount > buf.length) {
            do {
                int rest = buf.length - count;
                System.arraycopy(c, off, buf, count, rest);
                count = buf.length;
                flush();
                len -= rest;
                off += rest;
            } while (len > buf.length);
            newcount = len;
        }
        System.arraycopy(c, off, buf, count, len);
        count = newcount;
    }

    /**
     * 序列化字符串
     * @param text
     */
    public void write(String text) {
        if (text == null) {
            writeNull();
            return;
        }

        write(text, 0, text.length());
    }

    /**
     * 序列化 Null
     */
    public void writeNull() {
        write("null");
    }

    /**
     * 序列化字符串的直接实现方法
     * 基本原理：直接在 buf 末尾添加字符串
     * @param str
     * @param off
     * @param len
     */
    public void write(String str, int off, int len){
        /** 计算总共字符串长度 */
        int newcount = count + len;

        /** 存储空间充足，直接将str[off, off + len) 拷贝到buf[count, ...]中*/
        str.getChars(off, off + len, buf, count);
        count = newcount;
    }

    /**
     * 序列化整型数字
     * 基本思路: 先获取数字长度，再写入到输出流
     * @param i
     */
    public void writeInt(int i){
        /** 根据数字判断占用的位数，负数会多一位用于存储字符`-` */
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);

        int newcount = count + size;
        /** 如果当前存储空间不够, 本抽离代码采用固定容量, 空间不够报错 */
        if (newcount > buf.length) {
            char[] chars = new char[size];
            /** 将整数i转换成单字符并存储到chars数组 */
            getChars(i, size, chars);
            write(chars, 0, chars.length);
            return;
        }

        /** 如果buffer空间够，直接将字符写到buffer中 */
        getChars(i, newcount, buf);

        /** 重新计数buffer中字符数 */
        count = newcount;
    }

    /**
     * 序列化浮点类型数字
     * 基本思路: 先转换为字符串，然后在输出到输出流中。
     * @param doubleValue
     * @param checkWriteClassName
     */
    public void writeDouble(double doubleValue) {
        /** 如果doubleValue不合法或者是无穷数，调用writeNull */
        if (Double.isNaN(doubleValue)
                || Double.isInfinite(doubleValue)) {
            writeNull();
        } else {
            /** 将高精度double转换为字符串 */
            String doubleText = Double.toString(doubleValue);
            /** 启动WriteNullNumberAsZero特性，会将结尾.0去除 */
            if (doubleText.endsWith(".0")) {
                doubleText = doubleText.substring(0, doubleText.length() - 2);
            }

            /** 调用字符串输出方法 */
            write(doubleText);
        }
    }

    /**
     * 序列化浮点类型数字
     * @param value
     * @param checkWriteClassName
     */
    public void writeFloat(float value) {
        /** 如果value不合法或者是无穷数，调用writeNull */
        if (Float.isNaN(value) //
                || Float.isInfinite(value)) {
            writeNull();
        } else {
            /** 将高精度float转换为字符串 */
            String floatText= Float.toString(value);
            /** 启动WriteNullNumberAsZero特性，会将结尾.0去除 */
            if (floatText.endsWith(".0")) {
                floatText = floatText.substring(0, floatText.length() - 2);
            }

            write(floatText);
        }
    }

    /**
     * 序列化 Boolean
     * @param value
     */
    public void write(boolean value) {
        if (value) {
            /** 输出true字符串 */
            write("true");
        } else {
            /** 输出false字符串 */
            write("false");
        }
    }

    public void writeString(String text) {
        writeStringWithDoubleQuote(text, (char) 0);
    }

    /**
     * 序列化字段名称
     * @param key
     * @param checkSpecial
     */
    public void writeFieldName(String key, boolean checkSpecial) {
        if (key == null) {
            write("null:");
            return;
        }

        writeStringWithDoubleQuote(key, ':');
    }

    /**
     * 序列化包含特殊字符的字符串
     * @param text
     * @param seperator
     */
    public void writeStringWithDoubleQuote(String text, final char seperator) {
        if (text == null) {
            /** 如果字符串为空，输出null字符串 */
            writeNull();
            if (seperator != 0) {
                /** 如果分隔符不为空白字符' '，输出分隔符 */
                write(seperator);
            }
            return;
        }

        int len = text.length();
        int newcount = count + len + 2;
        if (seperator != 0) {
            newcount++;
        }

        int start = count + 1;
        int end = start + len;

        buf[count] = '\"';
        /** buffer能够容纳字符串，直接拷贝text到buf缓冲数组 */
        text.getChars(0, len, buf, start);

        count = newcount;

        /** 追加引用符号 */
        if (seperator != 0) {
            buf[count - 2] = '\"';
            buf[count - 1] = seperator;
        } else {
            buf[count - 1] = '\"';
        }
    }

    final static int[]  sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };
    /**
     * 根据传入的整数判断数字的位数
     * 取自源代码的 IOUtil
     * @param x 整数
     * @return
     */
    public int stringSize(int x) {
        for (int i = 0;; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    final static char[] digits    = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    /**
     * 将整数i转换成单字符并存储到chars数组
     * 取自源代码的 IOUtil
     * @param i
     * @param index
     * @param buf
     */
    public void getChars(int i, int index, char[] buf) {
        int q, r, p = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        for (;;) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1)); // r = i-(q*10) ...
            buf[--p] = digits[r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf[--p] = sign;
        }
    }

    public void flush() {
        if (writer == null) {
            return;
        }

        try {
            writer.write(buf, 0, count);
            writer.flush();
        } catch (IOException e) {
            throw new JSONException(e.getMessage(), e);
        }
        count = 0;
    }

    public void close() {
        if (writer != null && count > 0) {
            flush();
        }
        if (buf.length <= 1024 * 128) {
            bufLocal.set(buf);
        }

        this.buf = null;
    }

    public String toString() {
        return new String(buf, 0, count);
    }
}
