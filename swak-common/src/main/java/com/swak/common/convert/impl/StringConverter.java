package com.swak.common.convert.impl;

import com.swak.common.convert.ConvertUtil;
import com.swak.common.convert.Converter;
import com.swak.common.util.GetterUtil;

/**
 * StringConverter.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class StringConverter implements Converter<String> {

    @Override
    public String convert(Object value, String defaultValue, String... dataFormats) {
        return ConvertUtil.toStr(value, defaultValue,dataFormats);
    }

    @Override
    public String convertToStr(String value, String defaultValue, String... dataFormats) {
        return GetterUtil.getString(value, defaultValue);
    }
}
