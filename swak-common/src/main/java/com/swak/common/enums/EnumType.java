package com.swak.common.enums;


import com.swak.common.dto.base.I18nCode;
import com.swak.common.i18n.I18nDict;
import com.swak.common.util.StringPool;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author colley.ma
 * @since 2022/12/1 11:25
 */
public interface EnumType extends I18nCode {

    String getValue();

    String getName();

    @Override
    default String fallback() {
        return getName();
    }

    /**
     * 是否隐藏
     */
    default boolean isHidden() {
        return false;
    }

    default int order() {
        return 0;
    }

    default String category() {
        return StringPool.EMPTY;
    }

    @Override
    default String getAlias() {
        return StringPool.EMPTY;
    }

    default String getI18nName(Object... args) {
        if (this instanceof Enum) {
            Enum enumConstant = (Enum) this;
            return I18nDict.getMessage(enumConstant, getName(), args);
        }
        if (ArrayUtils.isNotEmpty(args)) {
            return MessageFormat.format(getName(), args);
        }
        return getName();
    }

    default String getI18nAlias(Object... args) {
        if (this instanceof Enum) {
            Enum enumConstant = (Enum) this;
            String codeKey = I18nDict.getMessageKey(enumConstant) + ".Alias";
            return I18nDict.getMessage(codeKey, getAlias(), args);
        }
        if (ArrayUtils.isNotEmpty(args)) {
            return MessageFormat.format(getAlias(), args);
        }
        return getAlias();
    }

    default boolean eq(String value) {
        return Objects.equals(value, getValue());
    }

    default boolean eq(Integer value) {
        if (Objects.isNull(value)) {
            return false;
        }
        return eq(value.toString());
    }

    default boolean filter(String name) {
        String trimName = StringUtils.trim(name);
        if (Objects.equals(getName(), trimName)) {
            return true;
        }
        if (this instanceof Enum) {
            Enum enumConstant = (Enum) this;
            String code = I18nDict.getMessageKey(enumConstant);
            String localeName = I18nDict.getMessage(code, null, getName(), Locale.ENGLISH);
            return Objects.equals(trimName, StringUtils.trim(localeName));
        }
        return false;
    }

    /**
     * 单个code精确匹配单个枚举，直接返回枚举
     *
     * @param value  枚举code
     * @param clazz 枚举实体类
     * @param <E>   枚举类型
     * @return 返回对应枚举
     */
    static <E extends Enum<E> & EnumType> E getEnum(String value, Class<E> clazz) {
        return getEnum(a -> Objects.equals(a.getValue(), value), clazz);
    }

    /**
     * 单个code精确匹配单个枚举，枚举二次处理
     * @param value     枚举code
     * @param clazz    实体类
     * @param function 二次函数处理
     * @param <E>      枚举类型
     * @param <N>      返回类型
     * @return 返回二次处理枚举
     */
    static <N, E extends Enum<E> & EnumType> N getEnumFun(String value, Class<E> clazz, Function<E, N> function) {
        return getEnumFun(a -> Objects.equals(a.getValue(), value), clazz, function);
    }

    /**
     * 指定条件匹配单个枚举，直接返回枚举
     *
     * @param predicate 判断条件
     * @param clazz     实体类
     * @param <E>       枚举类型
     * @return 返回二次处理枚举
     */
    static <E extends Enum<E> & EnumType> E getEnum(Predicate<E> predicate, Class<E> clazz) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).findFirst().orElse(null);
    }

    /**
     * 指定条件匹配单个枚举，枚举二次处理
     *
     * @param predicate 判断条件
     * @param clazz     实体类
     * @param function  二次函数处理
     * @param <E>       枚举类型
     * @param <N>       返回类型
     * @return 返回二次处理枚举
     */
    static <N, E extends Enum<E> & EnumType> N getEnumFun(Predicate<E> predicate, Class<E> clazz, Function<E, N> function) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).findFirst().map(function).orElse(null);
    }

    /**
     * 指定条件匹配单个枚举，直接返回枚举
     *
     * @param predicate 判断条件
     * @param clazz     实体类
     * @param <E>       枚举类型
     * @return 返回枚举集合
     */
    static <E extends Enum<E> & EnumType> List<E> getEnums(Predicate<E> predicate, Class<E> clazz) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).collect(Collectors.toList());
    }


    /**
     * 指定条件匹配单个枚举，枚举二次处理
     *
     * @param predicate 判断条件
     * @param clazz     实体类
     * @param function  二次函数处理
     * @param <E>       枚举类型
     * @param <N>       返回类型
     * @return 返回二次处理枚举集合
     */
    static <N, E extends Enum<E> & EnumType> List<N> getEnumsFun(Predicate<E> predicate, Class<E> clazz, Function<E, N> function) {
        EnumSet<E> all = EnumSet.allOf(clazz);
        return all.stream().filter(predicate).map(function).collect(Collectors.toList());
    }
}
