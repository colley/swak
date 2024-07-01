package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * CharacterConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class CharacterConverter implements Converter<Character> {

    @Override
    public Character convert(Object value, Character defaultValue, String... dataFormats) {
        if (value instanceof Boolean) {
            return (Boolean) value ? (char) 1 : (char) 0;
        }
        final String valueStr = ConvertUtil.toStr(value,dataFormats);
        if (StringUtils.isNotBlank(valueStr)) {
            return valueStr.charAt(0);
        }
        return null;
    }

    @Override
    public String convertToStr(Character value, String defaultValue, String... dataFormats) {
        if (Objects.nonNull(value)) {
            return value.toString();
        }
        return defaultValue;
    }
}
