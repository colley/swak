package com.swak.common.i18n;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.helpers.MessageFormatter;

import java.text.MessageFormat;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class I18nMessageFormat {

    public static String format(String message, Object... args) {
        if (ArrayUtils.isEmpty(args)) {
            return message;
        }
        if (isJdkFormat(message)) {
            return MessageFormat.format(message, args);
        }
        if (isSlf4jFormat(message)) {
            return MessageFormatter.arrayFormat(message, args).getMessage();
        }
        if (isStrFormat(message)) {
            return String.format(message, args);
        }
        return message;
    }

    private static boolean isJdkFormat(String message) {
        return message.contains("{0}");
    }

    private static boolean isStrFormat(String message) {
        return message.contains("%s");
    }

    private static boolean isSlf4jFormat(String message) {
        return message.contains("{}");
    }

    public static void main(String[] args) {
        Object[] saaa = new Object[]{"计时器","1"};
        System.out.println(format("{}({})",saaa));
        System.out.println(format("{0}({1})",saaa));
        System.out.println(format("%s(%s)",saaa));
    }
}
