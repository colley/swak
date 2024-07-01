package com.swak.jdbc.toolkit.support;

import lombok.Data;

import java.lang.reflect.Field;

/**
 * 反射字段缓存
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:26
 **/
@Data
public class FieldCache {

    public Field field;

    public Class<?> type;
}
