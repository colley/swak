package com.swak.jdbc.conditions;

import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;

import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public interface QueryJoin<Children, Entity> extends StringJoin<Children, Entity> {

    /**
     * left join
     *
     * @param clazz 关联的实体类
     * @param left  条件
     * @param right 条件
     */
    default <T, X> Children leftJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.LEFT_JOIN, clazz, left, right);
    }

    /**
     * left join
     *
     * @param clazz      关联的实体类
     * @param left       条件
     * @param rightAlias 条件字段别名
     * @param right      条件
     */
    default <T, X> Children leftJoin(Class<T> clazz, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.LEFT_JOIN, clazz, left, rightAlias, right);
    }

    /**
     * left join 多条件
     * <p>
     * 例 leftJoin(UserDO.class, on -> on.eq(UserDO::getId,UserAddressDO::getUserId).le().gt()...)
     *
     * @param clazz    关联实体类
     * @param function 条件`
     */
    default <T> Children leftJoin(Class<T> clazz, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.LEFT_JOIN, clazz, function);
    }

    /**
     * left join
     *
     * @param clazz 关联的实体类
     * @param left  条件
     * @param right 条件
     */
    default <T, X> Children leftJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.LEFT_JOIN, clazz, left, right, ext);
    }

    /**
     * left join 多条件
     * <p>
     * 例 leftJoin(UserDO.class, on -> on.eq(UserDO::getId,UserAddressDO::getUserId).le().gt()...)
     *
     * @param clazz    关联实体类
     * @param consumer 条件
     */
    default <T> Children leftJoin(Class<T> clazz, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.LEFT_JOIN, clazz, consumer);
    }

    /**
     * left join
     *
     * @param clazz 关联的实体类
     * @param left  条件
     * @param right 条件
     */
    default <T, X> Children leftJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.LEFT_JOIN, clazz, alias, left, right);
    }

    /**
     * left join
     *
     * @param clazz      关联的实体类
     * @param rightAlias 条件字段别名
     * @param left       条件
     * @param right      条件
     */
    default <T, X> Children leftJoin(Class<T> clazz, String alias, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.LEFT_JOIN, clazz, alias, left, rightAlias, right);
    }

    /**
     * left join 多条件
     * <p>
     * 例 leftJoin(UserDO.class, on -> on.eq(UserDO::getId,UserAddressDO::getUserId).le().gt()...)
     *
     * @param clazz    关联实体类
     * @param function 条件
     */
    default <T> Children leftJoin(Class<T> clazz, String alias, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.LEFT_JOIN, clazz, alias, function);
    }

    /**
     * left join
     *
     * @param clazz 关联的实体类
     * @param left  条件
     * @param right 条件
     */
    default <T, X> Children leftJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.LEFT_JOIN, clazz, alias, left, right, ext);
    }

    /**
     * left join 多条件
     * <p>
     * 例 leftJoin(UserDO.class, on -> on.eq(UserDO::getId,UserAddressDO::getUserId).le().gt()...)
     *
     * @param clazz    关联实体类
     * @param consumer 条件
     */
    default <T> Children leftJoin(Class<T> clazz, String alias, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.LEFT_JOIN, clazz, alias, consumer);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, left, right);

    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, left, rightAlias, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children rightJoin(Class<T> clazz, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, function);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, left, right, ext);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, consumer);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, String alias, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, alias, left, rightAlias, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children rightJoin(Class<T> clazz, String alias, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, alias, function);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, alias, left, right, ext);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children rightJoin(Class<T> clazz, String alias, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.RIGHT_JOIN, clazz, alias, consumer);
    }


    /**
     * ignore 参考 left join
     */
    default <T, X> Children innerJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.INNER_JOIN, clazz, left, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children innerJoin(Class<T> clazz, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.INNER_JOIN, clazz, left, rightAlias, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children innerJoin(Class<T> clazz, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.INNER_JOIN, clazz, function);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children innerJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.INNER_JOIN, clazz, left, right, ext);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children innerJoin(Class<T> clazz, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.INNER_JOIN, clazz, consumer);
    }


    /**
     * ignore 参考 left join
     */
    default <T, X> Children innerJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.INNER_JOIN, clazz, alias, on -> on.eq(left, right));
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children innerJoin(Class<T> clazz, String alias, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.INNER_JOIN, clazz, alias, function);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children innerJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.INNER_JOIN, clazz, alias, left, right, ext);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children innerJoin(Class<T> clazz, String alias, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.INNER_JOIN, clazz, alias, consumer);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children fullJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.FULL_JOIN, clazz, left, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children fullJoin(Class<T> clazz, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.FULL_JOIN, clazz, left, rightAlias, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children fullJoin(Class<T> clazz, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.FULL_JOIN, clazz, function);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children fullJoin(Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.FULL_JOIN, clazz, left, right, ext);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children fullJoin(Class<T> clazz, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.FULL_JOIN, clazz, consumer);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children fullJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(SqlKeyword.FULL_JOIN, clazz, alias, left, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children fullJoin(Class<T> clazz, String alias, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(SqlKeyword.FULL_JOIN, clazz, alias, left, rightAlias, right);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children fullJoin(Class<T> clazz, String alias, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(SqlKeyword.FULL_JOIN, clazz, alias, function);
    }

    /**
     * ignore 参考 left join
     */
    default <T, X> Children fullJoin(Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(SqlKeyword.FULL_JOIN, clazz, alias, left, right, ext);
    }

    /**
     * ignore 参考 left join
     */
    default <T> Children fullJoin(Class<T> clazz, String alias, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(SqlKeyword.FULL_JOIN, clazz, alias, consumer);
    }

    /**
     * 自定义连表关键词
     * 调用此方法 keyword 前后需要带空格 比如 " LEFT JOIN "  " RIGHT JOIN "
     * <p>
     * 查询基类 可以直接调用此方法实现以上所有功能
     *
     * @param keyWord 连表关键字
     * @param clazz   连表实体类
     * @param left    关联条件
     * @param right   扩展 用于关联表的 select 和 where
     */
    default <T, X> Children join(SqlKeyword keyWord, Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(keyWord, clazz, on -> on.eq(left, right));
    }

    /**
     * 自定义连表关键词
     * <p>
     * 查询基类 可以直接调用此方法实现以上所有功能
     *
     * @param keyWord 连表关键字
     * @param clazz   连表实体类
     * @param left    关联条件
     * @param right   扩展 用于关联表的 select 和 where
     */
    default <T, X> Children join(SqlKeyword keyWord, Class<T> clazz, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(keyWord, clazz, on -> on.eq(left, rightAlias,right));
    }

    /**
     * 自定义连表关键词
     * <p>
     * 例 leftJoin(UserDO.class, on -> on.eq(UserDO::getId,UserAddressDO::getUserId).le().gt()...)
     *
     * @param clazz    关联实体类
     * @param function 条件
     */
    default <T> Children join(SqlKeyword keyWord, Class<T> clazz, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(keyWord, clazz, (on, e) -> function.apply(on));
    }

    /**
     * 自定义连表关键词
     *
     * @param clazz 关联的实体类
     * @param left  条件
     * @param right 条件
     */
    default <T, X> Children join(SqlKeyword keyWord, Class<T> clazz, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(keyWord, clazz, (on, e) -> {
            on.eq(left, right);
            ext.apply(e);
        });
    }

    /**
     * 自定义连表关键词
     * 调用此方法 keyword 前后需要带空格 比如 " LEFT JOIN "  " RIGHT JOIN "
     * <p>
     * 查询基类 可以直接调用此方法实现以上所有功能
     *
     * @param keyWord 连表关键字
     * @param clazz   连表实体类
     * @param left    关联条件
     * @param right   扩展 用于关联表的 select 和 where
     */
    default <T, X> Children join(SqlKeyword keyWord, Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right) {
        return join(keyWord, clazz, alias, on -> on.eq(left, right));
    }

    /**
     * 自定义连表关键词
     * 调用此方法 keyword 前后需要带空格 比如 " LEFT JOIN "  " RIGHT JOIN "
     * <p>
     * 查询基类 可以直接调用此方法实现以上所有功能
     *
     * @param keyWord 连表关键字
     * @param clazz   连表实体类
     * @param left    关联条件
     * @param right   扩展 用于关联表的 select 和 where
     */
    default <T, X> Children join(SqlKeyword keyWord, Class<T> clazz, String alias, SFunction<T, ?> left, String rightAlias, SFunction<X, ?> right) {
        return join(keyWord, clazz, alias, on -> on.eq(left, rightAlias, right));
    }

    /**
     * 自定义连表关键词
     * <p>
     * 例 leftJoin(UserDO.class, on -> on.eq(UserDO::getId,UserAddressDO::getUserId).le().gt()...)
     *
     * @param clazz    关联实体类
     * @param function 条件
     */
    default <T> Children join(SqlKeyword keyWord, Class<T> clazz, String alias, WrapperFunction<AbstractLambdaWrapper<Entity, ?>> function) {
        return join(keyWord, clazz, alias, (on, e) -> function.apply(on));
    }

    /**
     * 自定义连表关键词
     *
     * @param clazz 关联的实体类
     * @param left  条件
     * @param right 条件
     */
    default <T, X> Children join(SqlKeyword keyWord, Class<T> clazz, String alias, SFunction<T, ?> left, SFunction<X, ?> right, WrapperFunction<Children> ext) {
        return join(keyWord, clazz, alias, (on, e) -> {
            on.eq(left, right);
            ext.apply(e);
        });
    }

    /**
     * 内部使用, 不建议直接调用
     */
    default <T> Children join(SqlKeyword keyWord, Class<T> clazz, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer) {
        return join(keyWord, clazz, null, consumer);
    }

    /**
     * 内部使用, 不建议直接调用
     */
    <T> Children join(SqlKeyword keyWord, Class<T> clazz, String alias, BiConsumer<AbstractLambdaWrapper<Entity, ?>, Children> consumer);
}
