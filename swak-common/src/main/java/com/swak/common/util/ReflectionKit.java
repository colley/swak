package com.swak.common.util;

import com.swak.common.exception.SwakAssert;
import com.swak.common.exception.SwakException;
import com.swak.common.i18n.I18nMessageFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * ReflectionKit.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class ReflectionKit {

    private static final Map<Class<?>, List<Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();
    /**
     * 反射 method 方法名，例如 setVersion
     */
    @Deprecated
    public static String setMethodCapitalize(Field field, final String str) {
        return GetterUtil.concatCapitalize("set", str);
    }

    /**
     * 获取字段值
     */
    public static Object getFieldValue(Object entity, String fieldName) {
        Class<?> cls = entity.getClass();
        Map<String, Field> fieldMaps = getFieldMap(cls);
        try {
            Field field = fieldMaps.get(fieldName);
            SwakAssert.notNull(field, I18nMessageFormat.format("Error: NoSuchField in %s for %s.  Cause:", cls.getSimpleName(), fieldName));
            field.setAccessible(true);
            return field.get(entity);
        } catch (ReflectiveOperationException e) {
            throw new SwakException(I18nMessageFormat.format("Error: Cannot read field in %s.  Cause:", cls.getSimpleName()), e);
        }
    }

    /**
     * 反射对象获取泛型
     */
    public static Class<?> getSuperClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            log.warn(String.format("Warn: %s's superclass not ParameterizedType", clazz.getSimpleName()));
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            log.warn(String.format("Warn: Index: %s, Size of %s's Parameterized Type: %s .", index,
                    clazz.getSimpleName(), params.length));
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            log.warn(String.format("Warn: %s not set the actual class on superclass generic parameter",
                    clazz.getSimpleName()));
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    /**
     * 获取该类的所有属性列表
     */
    public static Map<String, Field> getFieldMap(Class<?> clazz) {
        List<Field> fieldList = getFieldList(clazz);
        return CollectionUtils.isNotEmpty(fieldList) ? fieldList.stream().collect(Collectors.toMap(Field::getName, field -> field)) : Collections.emptyMap();
    }

    /**
     * 获取该类的所有属性列表
     */
    public static List<Field> getFieldList(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return Collections.emptyList();
        }
        return CLASS_FIELD_CACHE.computeIfAbsent(clazz, k -> {
            Field[] fields = k.getDeclaredFields();
            List<Field> superFields = new ArrayList<>();
            Class<?> currentClass = k.getSuperclass();
            while (currentClass != null) {
                Field[] declaredFields = currentClass.getDeclaredFields();
                Collections.addAll(superFields, declaredFields);
                currentClass = currentClass.getSuperclass();
            }
            /* 排除重载属性 */
            Map<String, Field> fieldMap = excludeOverrideSuperField(fields, superFields);
            /*
             * 重写父类属性过滤后处理忽略部分，支持过滤父类属性功能
             * 场景：中间表不需要记录创建时间，忽略父类 createTime 公共属性
             * 中间表实体重写父类属性 ` private transient Date createTime; `
             */
            return fieldMap.values().stream()
                    /* 过滤静态属性 */
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    /* 过滤 transient关键字修饰的属性 */
                    .filter(f -> !Modifier.isTransient(f.getModifiers()))
                    .collect(Collectors.toList());
        });
    }


    /**
     * 排序重置父类属性
     */
    public static Map<String, Field> excludeOverrideSuperField(Field[] fields, List<Field> superFieldList) {
        // 子类属性
        Map<String, Field> fieldMap = Stream.of(fields).collect(toMap(Field::getName, identity(),
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new));
        superFieldList.stream().filter(field -> !fieldMap.containsKey(field.getName()))
                .forEach(f -> fieldMap.put(f.getName(), f));
        return fieldMap;
    }
}
