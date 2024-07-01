package com.swak.core.converter;

public interface Converter<O, I> {

    public O convert(I result);
}
