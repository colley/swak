package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.util.GetterUtil;

import java.util.Objects;

/**
 * BooleanConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class BooleanConverter implements Converter<Boolean> {

    @Override
    public Boolean convert(Object value, Boolean defaultValue,String... dataFormats) {
        if(Objects.isNull(value)){
            return defaultValue;
        }
        if (value instanceof Number) {
            // 0为false，其它数字为true
            return 0 != ((Number) value).doubleValue();
        }
        return GetterUtil.getBoolean(ConvertUtil.toStr(value,dataFormats), defaultValue);
    }

    @Override
    public String convertToStr(Boolean value, String defaultValue,String... dataFormats) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return value.toString();
    }
}
