package com.swak.common.convert;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public interface Converter<T> {

    T convert(Object value, T defaultValue, String... dataFormats);

    default T convert(Object value, T defaultValue) {
        return convert(value, defaultValue, null);
    }

    default T convert(Object value) {
        return convert(value, null);
    }

    String convertToStr(T value, String defaultValue, String... dataFormat);

    default String convertToStr(T value) {
        return convertToStr(value, null);
    }

    static String getGlobalDateFormat() {
        return FormatRegisterFactory.getDateFormat();
    }
}
