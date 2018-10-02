package com.kdocke.fastjson.parser;

/**
 * JSONToken
 * @author Kdocke[kdocked@gmail.com]
 * @create 2018/9/17 - 17:11
 */
public class JSONToken {

    /** 1 关联到 error */
    public final static int ERROR                = 1;

    /** 2 关联到 int */
    public final static int LITERAL_INT          = 2;

    /** 3 关联到 float */
    public final static int LITERAL_FLOAT        = 3;

    /** 4 关联到 string */
    public final static int LITERAL_STRING       = 4;

    /** 5 关联到 iso8601 */
    public final static int LITERAL_ISO8601_DATE = 5;

    /** 6 关联到 true */
    public final static int TRUE                 = 6;

    /** 7 关联到 false */
    public final static int FALSE                = 7;

    /** 8 关联到 null */
    public final static int NULL                 = 8;

    /** 9 关联到 new */
    public final static int NEW                  = 9;

    /** 10 关联到 ( */
    public final static int LPAREN               = 10;

    /** 11 关联到 ) */
    public final static int RPAREN               = 11;

    /** 12 关联到 { */
    public final static int LBRACE               = 12;

    /** 13 关联到 } */
    public final static int RBRACE               = 13;

    /** 14 关联到 [ */
    public final static int LBRACKET             = 14;

    /** 15 关联到 ] */
    public final static int RBRACKET             = 15;

    /** 16 关联到 , */
    public final static int COMMA                = 16;

    /** 17 关联到 : */
    public final static int COLON                = 17;

    /** 18 关联到 ident */
    public final static int IDENTIFIER           = 18;

    /** 19 关联到 fieldName */
    public final static int FIELD_NAME           = 19;

    /** 20 关联到 EOF */
    public final static int EOF                  = 20;

    /** 21 关联到 Set */
    public final static int SET                  = 21;

    /** 22 关联到 TreeSet */
    public final static int TREE_SET             = 22;

    /** 23 关联到 undefined */
    public final static int UNDEFINED            = 23; // undefined

    /** 24 关联到 ; */
    public final static int SEMI                 = 24;

    /** 25 关联到 . */
    public final static int DOT                  = 25;

    /** 26 关联到 hex */
    public final static int HEX                  = 26;

    public static String name(int value){
        switch (value){
            case ERROR:
                return "error";
            case LITERAL_INT:
                return "int";
            case LITERAL_FLOAT:
                return "float";
            case LITERAL_STRING:
                return "string";
            case LITERAL_ISO8601_DATE:
                return "iso8601";
            case TRUE:
                return "true";
            case FALSE:
                return "false";
            case NULL:
                return "null";
            case NEW:
                return "new";
            case LPAREN:
                return "(";
            case RPAREN:
                return ")";
            case LBRACE:
                return "{";
            case RBRACE:
                return "}";
            case LBRACKET:
                return "[";
            case RBRACKET:
                return "]";
            case COMMA:
                return ",";
            case COLON:
                return ":";
            case SEMI:
                return ";";
            case DOT:
                return ".";
            case IDENTIFIER:
                return "ident";
            case FIELD_NAME:
                return "fieldName";
            case EOF:
                return "EOF";
            case SET:
                return "Set";
            case TREE_SET:
                return "TreeSet";
            case UNDEFINED:
                return "undefined";
            case HEX:
                return "hex";
            default:
                return "Unknown";
        }
    }

}
