package com.swak.common.exception;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;
import com.swak.common.i18n.I18nMessageUtil;
import com.swak.common.util.StringPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Assert.java  断言
 *
 * @author colley.ma
 * @since 2022/9/20 14:25
 */
public abstract class SwakAssert {

    public static void state(boolean expression, String code, String message) {
        if (!expression) {
            throw new ArgumentException(I18nMessageUtil.getMessage(code, message));
        }
    }

    public static void state(boolean expression,String message) {
        if (!expression) {
            throw new ArgumentException(message);
        }
    }

    public static void state(boolean expression, Supplier<String> message) {
        if (!expression) {
            throw new ArgumentException(BasicErrCode.INVALID_PARAMETER, message.get(),null);
        }
    }

    public static void state(boolean expression, String message,IResultCode errorCode) {
        if (!expression) {
            throw new ArgumentException(errorCode, message,null);
        }
    }

    public static void state(boolean expression, IResultCode errorCode, Object... args) {
        if (!expression) {
            throw new ArgumentException(errorCode, args);
        }
    }

    public static void state(boolean expression) {
        state(expression, StringPool.EMPTY, "this expression must be true");
    }


    public static void notNull(Object object, String message,IResultCode errorCode) {
        if (object == null) {
            throw new ArgumentException(errorCode, message,null);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_NULL, message,null);
        }
    }

    public static void notNull(Object object, IResultCode errorCode, Object... args) {
        if (object == null) {
            throw new ArgumentException(errorCode, args);
        }
    }


    public static void notNull(Object object, Supplier<String> msgSupplier) {
        if (object == null) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_NULL,msgSupplier.get(),null);
        }
    }

    public static void notNull(Object object) {
        if (object == null) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_NULL);
        }
    }

    public static void notHasNull(String message, Object... objects) {
        boolean hasNull = Stream.of(objects).anyMatch(Objects::isNull);
        if (hasNull) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_NULL,message,null);
        }
    }

    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection, BasicErrCode.PARAMETER_NOT_EMPTY);
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_EMPTY,message,null);
        }
    }

    public static void notEmpty(Collection<?> collection, IResultCode errorCode, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ArgumentException(errorCode, args);
        }
    }

    public static void notEmpty(Collection<?> collection, String message,IResultCode errorCode, Object... args) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ArgumentException(errorCode, message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message) {
        if (MapUtils.isEmpty(map)) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_EMPTY,message,null);
        }
    }

    public static void notEmpty(Map<?, ?> map, IResultCode errorCode, Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new ArgumentException(errorCode, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, String message, IResultCode errorCode, Object... args) {
        if (MapUtils.isEmpty(map)) {
            throw new ArgumentException(errorCode, message, args);
        }
    }

    public static void notEmpty(Map<?, ?> map) {
        notEmpty(map, "this map must not be empty");
    }

    public static void notBlank(CharSequence css, String message) {
        if (StringUtils.isBlank(css)) {
            throw new ArgumentException(BasicErrCode.PARAMETER_NOT_BLANK,message,null);
        }
    }

    public static void notBlank(CharSequence css, IResultCode errorCode, Object... args) {
        if (StringUtils.isBlank(css)) {
            throw new ArgumentException(errorCode, args);
        }
    }

    public static void notBlank(CharSequence css, String message,IResultCode errorCode, Object... args) {
        if (StringUtils.isBlank(css)) {
            throw new ArgumentException(errorCode, message, args);
        }
    }
}
