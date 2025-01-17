package  com.swak.jdbc.conditions;

import com.swak.common.util.StringPool;
import com.swak.jdbc.common.SharedBool;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.common.TableAssert;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.metadata.SelectCache;
import com.swak.jdbc.metadata.TableInfo;
import com.swak.jdbc.segments.AliasColumnSegment;
import com.swak.jdbc.segments.ClassColumnSegment;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.segments.ConstColumnSegment;
import com.swak.jdbc.toolkit.LambdaUtils;
import com.swak.jdbc.toolkit.TableHelper;
import com.swak.jdbc.toolkit.support.ColumnCache;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface SwakQuery<Children> extends Serializable {

    List<ColumnSegment> getSelectFrom();

    Children getChildren();

    SharedInteger getIndex();

    SharedBool getHasAlias();

    SharedString getAlias();

    default Children from(String tableName) {
        return from(tableName,null);
    }
    Children from(String tableName,String tableAlias);

    default <E> Children from(Class<E> entityClass) {
        TableInfo info = TableHelper.get(entityClass);
        TableAssert.hasTable(info, entityClass);
        return from(info.getTableName());
    }

    /**
     * 过滤查询的字段信息
     * <p>例1: 只要 java 字段名以 "test" 开头的             -> select(i -> i.getProperty().startsWith("test"))</p>
     * <p>例2: 只要 java 字段属性是 CharSequence 类型的     -> select(TableFieldInfo::isCharSequence)</p>
     * <p>例3: 只要 java 字段没有填充策略的                 -> select(i -> i.getFieldFill() == FieldFill.DEFAULT)</p>
     * <p>例4: 要全部字段                                   -> select(i -> true)</p>
     * <p>例5: 只要主键字段                                 -> select(i -> false)</p>
     *
     * @param predicate 过滤方式
     * @return children
     */
    default <E> Children selectFilter(Class<E> entityClass, Predicate<SelectCache> predicate) {
        TableInfo info = TableHelper.get(entityClass);
        TableAssert.hasTable(info, entityClass);
        List<SelectCache> cacheList = ColumnCache.getListField(entityClass);
        cacheList.stream().filter(predicate).collect(Collectors.toList()).forEach(
                i -> getSelectFrom().add(new ClassColumnSegment(i, getIndex().getValue(), getHasAlias().isTrue(), getAlias().getValue())));
        return getChildren();
    }


    @SuppressWarnings("unchecked")
    <E> Children select(SFunction<E, ?>... columns);

    /**
     * String 查询
     *
     * @param columns 列
     */
    default Children select(String... columns) {
        getSelectFrom().addAll(Arrays.stream(columns).map(i ->
                AliasColumnSegment.alias(i,getHasAlias().isTrue(), getAlias().getValue())).collect(Collectors.toList()));
        return getChildren();
    }


    default Children selectConst(Object... values) {
        getSelectFrom().addAll(Arrays.stream(values).map(i ->ConstColumnSegment.constant(i)).collect(Collectors.toList()));
        return getChildren();
    }

    /**
     * String 查询
     *
     * @param column 列
     */
    default <E> Children selectAs(String column, SFunction<E, ?> alias) {
        getSelectFrom().add(AliasColumnSegment.as(column,getHasAlias().isTrue(), LambdaUtils.getName(alias), getAlias().getValue()));
        return getChildren();
    }

    /**
     * String 查询
     *
     * @param column 列
     */
    default <E, X> Children selectAs(String index, SFunction<E, ?> column, SFunction<X, ?> alias) {
        Map<String, SelectCache> cacheMap = ColumnCache.getMapField(LambdaUtils.getEntityClass(column));
        SelectCache cache = cacheMap.get(LambdaUtils.getName(column));
        getSelectFrom().add(AliasColumnSegment.alias(
                index + StringPool.DOT + cache.getColumn() + SqlKeyword.AS.getKeyword() +
                        LambdaUtils.getName(alias),getAlias().getValue()));
        return getChildren();
    }


    /**
     * ignore
     */
    default <S, X> Children selectAs(SFunction<S, ?> column, SFunction<X, ?> alias) {
        return selectAs(column, LambdaUtils.getName(alias));
    }

    /**
     * 别名查询
     */
    default <S> Children selectAs(SFunction<S, ?> column, String alias) {
        Class<?> aClass = LambdaUtils.getEntityClass(column);
        Map<String, SelectCache> cacheMap = ColumnCache.getMapField(aClass);
        getSelectFrom().add(new ClassColumnSegment(cacheMap.get(LambdaUtils.getName(column)), getIndex().getValue(), alias, getHasAlias().isTrue(), getAlias().getValue()));
        return getChildren();
    }





    /**
     * 查询实体类全部字段
     */
    default Children selectAll(Class<?> clazz) {
        getSelectFrom().addAll(ColumnCache.getListField(clazz).stream().map(i ->
                new ClassColumnSegment(i, getIndex().getValue(), getHasAlias().getValue(), getAlias().getValue())).collect(Collectors.toList()));
        return from(clazz);
    }
}
