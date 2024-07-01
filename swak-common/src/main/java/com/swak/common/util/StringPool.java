package com.swak.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: StringPool.java
 * 字符串常量池
 * @author colley.ma
 * @since  2021年3月12日
 */
public interface StringPool {
    String EMPTY = "";
    String AMPERSAND = "&";
    String AMPERSAND_ENCODED = "&amp;";
    String APOSTROPHE = "'";
    String AT = "@";
    String BACK_SLASH = "\\";
    String BETWEEN = "BETWEEN";
    String CDATA_OPEN = "<![CDATA[";
    String CDATA_CLOSE = "]]>";

    String OPEN_BRACKET = "[";
    String CLOSE_BRACKET = "]";

    String DOUBLE_CLOSE_BRACKET = "]]";
    String DOUBLE_OPEN_BRACKET = "[[";

    String OPEN_BRACE = "{";
    String CLOSE_CURLY_BRACE = "}";
    String OPEN_CURLY_BRACE = "{";
    String OPEN_PARENTHESIS = "(";
    String CLOSE_PARENTHESIS = ")";
    String COLON = ":";
    String COLON2 = "::";
    String COMMA = ",";
    String COMMA_AND_SPACE = ", ";
    String DASH = "-";
    String DOUBLE_APOSTROPHE = "''";

    String DOUBLE_SLASH = "//";
    String EQUAL = "=";
    String GREATER_THAN = ">";
    String GREATER_THAN_OR_EQUAL = ">=";
    String FALSE = "false";
    String FORWARD_SLASH = "/";
    String FOUR_SPACES = "    ";
    String IS_NOT_NULL = "IS NOT NULL";
    String IS_NULL = "IS NULL";
    String LESS_THAN = "<";
    String LESS_THAN_OR_EQUAL = "<=";
    String LIKE = "LIKE";

    String NEW_LINE = "\n";

    String NOT_EQUAL = "!=";
    String NOT_LIKE = "NOT LIKE";
    String NULL = "null";


    String PERCENT = "%";
    String PIPE = "|";
    String PLUS = "+";
    String QUESTION = "?";
    String QUOTE = "\"";
    String RETURN = "\r";
    String SEMICOLON = ";";

    String SPACE = " ";
    String STAR = "*";
    String TILDE = "~";
    String TRUE = "true";
    String UNDERLINE = "_";
    String UTF8 = "UTF-8";
    String X_CHAR = "x";
    String AND = "and";
    String ASTERISK = "*";
    String DOLLAR = "$";
    String DOT = ".";
    String DOTDOT = "..";
    String DOT_CLASS = ".class";
    String DOT_JAVA = ".java";
    String DOT_XML = ".xml";
    String HASH = "#";
    String HAT = "^";
    String LEFT_CHEV = "<";
    String DOT_NEWLINE = ",\n";
    String N = "n";
    String NO = "no";

    String OFF = "off";
    String ON = "on";
    String QUESTION_MARK = "?";
    String EXCLAMATION_MARK = "!";
    String TAB = "\t";
    String RIGHT_BRACE = "}";
    String RIGHT_CHEV = ">";
    String SINGLE_QUOTE = "'";
    String BACKTICK = "`";
    String TILDA = "~";

    String UNDERSCORE = "_";
    String US_ASCII = "US-ASCII";
    String ISO_8859_1 = "ISO-8859-1";
    String Y = "y";
    String YES = "yes";
    String ONE = "1";
    String ZERO = "0";
    String DOLLAR_LEFT_BRACE = "${";
    String HASH_LEFT_BRACE = "#{";
    String CRLF = "\r\n";

    String HTML_NBSP = "&nbsp;";
    String HTML_QUOTE = "&quot;";
    String HTML_LT = "&lt;";
    String HTML_GT = "&gt;";

    String KEY = "swakc1$7@";

    // ---------------------------------------------------------------- array
    String[] EMPTY_ARRAY = new String[0];
    byte[] BYTES_NEW_LINE = StringPool.NEW_LINE.getBytes();
    boolean DEFAULT_BOOLEAN = false;
    Boolean[] DEFAULT_BOOLEAN_VALUES = new Boolean[0];
    double DEFAULT_DOUBLE = 0.0d;
    Double[] DEFAULT_DOUBLE_VALUES = new Double[0];
    float DEFAULT_FLOAT = 0.0f;
    Float[] DEFAULT_FLOAT_VALUES = new Float[0];
    int DEFAULT_INTEGER = 0;
    Integer[] DEFAULT_INTEGER_VALUES = new Integer[0];
    long DEFAULT_LONG = 0;
    Long[] DEFAULT_LONG_VALUES = new Long[0];
    short DEFAULT_SHORT = 0;
    Short[] DEFAULT_SHORT_VALUES = new Short[0];
    Map<String,Boolean> BOOLEANS = new HashMap<String,Boolean>(){{
        for (String s : new String[]{"true", "t", "y", "on", "1", "Yes"}) {
            put(s.toLowerCase(),Boolean.TRUE);
        }
        for (String s : new String[]{"false","off", "0","No"}) {
            put(s.toLowerCase(),Boolean.FALSE);
        }
    }};
}
