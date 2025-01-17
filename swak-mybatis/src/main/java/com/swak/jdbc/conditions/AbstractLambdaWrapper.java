package com.swak.jdbc.conditions;


import com.swak.jdbc.common.SharedBool;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.common.TableAssert;
import com.swak.jdbc.enums.SqlKeyword;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.metadata.TableInfo;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.toolkit.TableHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.BiConsumer;

import static com.swak.jdbc.enums.SqlKeyword.*;

public  abstract  class AbstractLambdaWrapper<T,Children extends AbstractLambdaWrapper<T,Children>>
        extends AbstractWrapper<T,Children> implements QueryJoin<Children, T>,SwakWrapper<T> {

    public AbstractLambdaWrapper() {
        super();
        this.initNeed();
    }

    /**
     * 推荐使用此构造方法
     */
    public AbstractLambdaWrapper(Class<T> clazz) {
        super();
        setEntityClass(clazz);
        tableList.setRootClass(clazz);
    }

    /**
     * 构造方法
     *
     * @param entity 主表实体
     */
    public AbstractLambdaWrapper(T entity) {
        this();
        setEntity(entity);
        if (entity != null) {
            setEntityClass((Class<T>) entity.getClass());
            tableList.setRootClass(entity.getClass());
        }
    }

    /* ************************* on语句重载 *************************** */
    @Override
    public <R, S> Children eq(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, EQ, rightAlias, val);
    }

    @Override
    public <R, S> Children ne(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, NE, rightAlias, val);
    }

    @Override
    public <R, S> Children gt(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, GT, rightAlias, val);
    }

    @Override
    public <R, S> Children ge(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, GE, rightAlias, val);
    }

    @Override
    public <R, S> Children lt(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, LT, rightAlias, val);
    }

    @Override
    public <R, S> Children le(boolean condition, String alias, SFunction<R, ?> column, String rightAlias, SFunction<S, ?> val) {
        return addCondition(condition, alias, column, LE, rightAlias, val);
    }
    /* ****************************************** **/



    @Override
    public <R> Children join(SqlKeyword keyWord, Class<R> clazz, String tableAlias, BiConsumer<AbstractLambdaWrapper<T, ?>, Children> consumer) {
        Integer oldIndex = this.getIndex().getValue();
        int newIndex = tableIndex.getValue();
        TableInfo info = TableHelper.get(clazz);
        TableAssert.hasTable(info,clazz);
        Children instance = instance(new SharedInteger(newIndex),keyWord, clazz,new SharedString(info.getTableName()));
        instance.isOn.setValue(true);
        instance.isMain.setValue(false);
        getOnWrappers().add(instance);
        if (StringUtils.isBlank(tableAlias)) {
            tableList.put(oldIndex, clazz, false, subTableAlias.getValue(), newIndex);
            instance.alias.setValue(subTableAlias.getValue());
            instance.hasAlias.setValue(false);
        } else {
            tableList.put(oldIndex, clazz, true, tableAlias, newIndex);
            instance.alias.setValue(tableAlias);
            instance.hasAlias.setValue(true);
        }
        tableIndex.incr();
        this.index.setValue(newIndex);
        SharedBool isM = new SharedBool(this.isMain.isTrue());
        this.isMain.setValue(false);
        consumer.accept(instance, typedThis);
        this.isMain.setValue(isM.getValue());
        this.index.setValue(oldIndex);
        return typedThis;
    }

    @Override
    public Children join(SqlKeyword keyWord, boolean condition, String joinSql) {
        if (condition) {
            Children wrapper = instanceEmpty();
            wrapper.from.setValue(joinSql);
            wrapper.sqlKeyword = keyWord;
            getOnWrappers().add(wrapper);
        }
        return typedThis;
    }


    @Override
    protected abstract Children instance(SharedInteger index, SqlKeyword keyWord, Class<?> joinClass, SharedString tableName);

    @Override
    protected abstract Children instanceEmpty();

    @Override
    protected void initNeed() {
        super.initNeed();
    }
}
