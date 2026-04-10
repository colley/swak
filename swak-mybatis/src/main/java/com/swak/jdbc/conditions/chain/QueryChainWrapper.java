package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.common.SharedBool;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.SwakQuery;
import com.swak.jdbc.conditions.query.QueryWrapper;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.spi.SwakJdbcTemplate;

import java.util.List;

/**
 * QueryChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class QueryChainWrapper<T> extends AbstractChainWrapper<T, String, QueryChainWrapper<T>, QueryWrapper<T>>
        implements ChainQuery<T>, SwakQuery<QueryChainWrapper<T>> {

    private final SwakJdbcTemplate baseMapper;

    public QueryChainWrapper(SwakJdbcTemplate baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new QueryWrapper<>();
    }

    public QueryChainWrapper(SwakJdbcTemplate baseMapper, T entity) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new QueryWrapper<>(entity);
    }

    public QueryChainWrapper(SwakJdbcTemplate baseMapper, Class<T> entityClass) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new QueryWrapper<>(entityClass);
    }

    @Override
    public SwakJdbcTemplate getBaseMapper() {
        return baseMapper;
    }

    @Override
    public QueryWrapper<T> getWrapper() {
        return wrapperChildren;
    }

    @Override
    public List<ColumnSegment> getSelectFrom() {
        return getWrapper().getSelectFrom();
    }

    @Override
    public QueryChainWrapper<T> getChildren() {
        return this;
    }

    @Override
    public SharedInteger getIndex() {
        return getWrapper().getIndex();
    }

    @Override
    public SharedBool getHasAlias() {
        return getWrapper().getHasAlias();
    }

    @Override
    public SharedString getAlias() {
        return getWrapper().getAlias();
    }


    @Override
    public QueryChainWrapper<T> from(String tableName, String tableAlias) {
        getWrapper().from(tableName,tableAlias);
        return this;
    }

    @Override
    public <E> QueryChainWrapper<T> select(SFunction<E, ?>... columns) {
        getWrapper().select(columns);
        return this;
    }
}
