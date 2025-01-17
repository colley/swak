package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.common.SharedBool;
import com.swak.jdbc.common.SharedInteger;
import com.swak.jdbc.common.SharedString;
import com.swak.jdbc.conditions.SwakQuery;
import com.swak.jdbc.conditions.query.LambdaQueryWrapper;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.segments.ColumnSegment;
import com.swak.jdbc.spi.SwakJdbcTemplate;

import java.util.List;

/**
 * LambdaChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class LambdaChainWrapper<T> extends AbstractChainWrapper<T, String, LambdaChainWrapper<T>, LambdaQueryWrapper<T>>
        implements ChainQuery<T>, SwakQuery<LambdaChainWrapper<T>> {

    private final SwakJdbcTemplate baseMapper;

    public LambdaChainWrapper(SwakJdbcTemplate baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaQueryWrapper<>();
    }

    public LambdaChainWrapper(SwakJdbcTemplate baseMapper, T entity) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaQueryWrapper<>(entity);
    }

    public LambdaChainWrapper(SwakJdbcTemplate baseMapper, Class<T> entityClass) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaQueryWrapper<>(entityClass);
    }

    @Override
    public SwakJdbcTemplate getBaseMapper() {
        return baseMapper;
    }

    @Override
    public LambdaQueryWrapper<T> getWrapper() {
        return wrapperChildren;
    }

    @Override
    public List<ColumnSegment> getSelectFrom() {
        return getWrapper().getSelectFrom();
    }

    @Override
    public LambdaChainWrapper<T> getChildren() {
        return this;
    }

    @Override
    public SharedInteger getIndex() {
        return getWrapper().getIndex();
    }

    @Override
    public SharedBool getHasAlias() {
        return  getWrapper().getHasAlias();
    }

    @Override
    public SharedString getAlias() {
        return  getWrapper().getAlias();
    }

    @Override
    public LambdaChainWrapper<T> from(String tableName, String tableAlias) {
        getWrapper().from(tableName,tableAlias);
        return null;
    }

    @Override
    public <E> LambdaChainWrapper<T> select(SFunction<E, ?>... columns) {
        getWrapper().select(columns);
        return this;
    }
}
