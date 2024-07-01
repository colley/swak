package com.swak.common.enums;

import com.swak.common.dto.base.I18nCode;
import com.swak.common.i18n.I18nMessageUtil;
import com.swak.common.util.StringPool;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * 返回code接口
 *
 * @author colley.ma
 * @since 2022/9/9 16:24
 */
public interface IResultCode extends I18nCode {

    Integer getCode();

    String getMsg();

    @Override
    default String fallback() {
        return getMsg();
    }
    
    @Override
    default String getAlias() {
        return StringPool.EMPTY;
    }

    default String getI18nMsg(Object... args) {
        if (this instanceof Enum) {
            Enum enumConstant = (Enum) this;
            return i18nMessage(I18nMessageUtil.getMessageKey(enumConstant), getMsg(), args);
        }
        return i18nMessage(getMsg(), args);
    }

    default boolean eq(String errCode) {
        return Objects.equals(String.valueOf(getCode()), errCode);
    }

    default boolean eq(Integer errCode) {
        return Objects.equals(getCode(), errCode);
    }


    static String i18nMessage(String key, String message, Object... args) {
        if (StringUtils.isEmpty(key)) {
            return i18nMessage(message, args);
        }
        return I18nMessageUtil.getMessage(key,message,args);
    }

    static String i18nMessage(String message, Object... args) {
        if (ArrayUtils.isNotEmpty(args)) {
            return MessageFormat.format(message, args);
        }
        return message;
    }
}
