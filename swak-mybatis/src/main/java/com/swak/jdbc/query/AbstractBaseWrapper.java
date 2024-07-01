package com.swak.jdbc.query;

import com.swak.common.util.StringEscape;
import com.swak.common.util.StringPool;
import com.swak.jdbc.ParamNameValuePairTranslator;
import com.swak.jdbc.ParamNameValuePairs;
import com.swak.jdbc.conditions.SwakWrapper;
import com.swak.jdbc.metadata.TableList;
import com.swak.jdbc.segments.WhereSegmentWrapper;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBaseWrapper<T, Children extends AbstractBaseWrapper<T, Children>>
        extends WhereSegmentWrapper<Children> implements SwakWrapper<T> {

    @Getter
    protected ParamNameValuePairs paramNameValuePairs;

    /**
     * ON sql wrapper集合
     */
    protected List<Children> onWrappers = new ArrayList<>();
    /**
     * ß
     * 数据库表映射实体类
     */
    private T entity;
    /**
     * 实体类型(主要用于确定泛型以及取TableInfo缓存)
     */
    private Class<T> entityClass;


    public AbstractBaseWrapper() {
        super();
    }

    @Override
    public T getEntity() {
        return entity;
    }

    public Children setEntity(T entity) {
        this.entity = entity;
        return typedThis;
    }

    @Override
    public Class<T> getEntityClass() {
        if (entityClass == null && entity != null) {
            entityClass = (Class<T>) entity.getClass();
        }
        return entityClass;
    }

    public Children setEntityClass(Class<T> entityClass) {
        if (entityClass != null) {
            this.entityClass = entityClass;
        }
        return typedThis;
    }

    @Override
    public String getSqlSegment(ParamNameValuePairs valuePairs) {
        return super.getSqlSegment(valuePairs) + lastSql.getValue();
    }

    @Override
    public String getSqlFirst() {
        if (StringUtils.isNotBlank(sqlFirst.getValue())) {
            return StringEscape.escapeRawString(sqlFirst.getValue());
        }
        return StringPool.EMPTY;
    }


    /**
     * 必要的初始化
     */
    @Override
    protected void initNeed() {
        super.initNeed();
        tableList = new TableList();
        tableList.setAlias(getAlias().getValue());
        paramNameValuePairs = new ParamNameValuePairTranslator(tableList);
    }

    @Override
    public void clear() {
        super.clear();
        this.entity = null;
        this.sqlKeyword = null;
        this.paramNameValuePairs.clear();
        this.tableIndex.setValue(1);
        this.isOn.setValue(false);
        this.isMain.setValue(true);
    }

    @Override
    @SuppressWarnings("all")
    public Children clone() {
        return SerializationUtils.clone(getChildren());
    }
}
