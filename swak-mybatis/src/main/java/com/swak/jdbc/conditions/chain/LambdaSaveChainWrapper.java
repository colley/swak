package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.conditions.update.LambdaSaveWrapper;
import com.swak.jdbc.conditions.update.SwakSave;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.spi.SwakJdbcTemplate;

import java.util.Map;

/**
 * LambdaSaveChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class LambdaSaveChainWrapper<T> extends AbstractChainWrapper<T, String, LambdaSaveChainWrapper<T>, LambdaSaveWrapper<T>>
        implements ChainSave<T>, SwakSave<LambdaSaveChainWrapper<T>, SFunction<T, ?>, T> {

    private final SwakJdbcTemplate baseMapper;

    public LambdaSaveChainWrapper(SwakJdbcTemplate baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaSaveWrapper<>();
    }

    public LambdaSaveChainWrapper(SwakJdbcTemplate baseMapper, T entity) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaSaveWrapper<>(entity);
    }

    public LambdaSaveChainWrapper(SwakJdbcTemplate baseMapper, Class<T> entityClass) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaSaveWrapper<>(entityClass);
    }

    @Override
    public SwakJdbcTemplate getBaseMapper() {
        return baseMapper;
    }

    @Override
    public LambdaSaveWrapper<T> getWrapper() {
        return wrapperChildren;
    }

    @Override
    public LambdaSaveChainWrapper<T> addColumn(SFunction<T, ?>... columns) {
        wrapperChildren.addColumn(columns);
        return this;
    }

    @Override
    public LambdaSaveChainWrapper<T> addValue(SFunction<T, ?> column, Object value) {
        wrapperChildren.addValue(column,value);
        return this;
    }

    @Override
    public LambdaSaveChainWrapper<T> addColumn(Map<SFunction<T, ?>, Object> columnValue) {
        wrapperChildren.addColumn(columnValue);
        return this;
    }

    @Override
    public LambdaSaveChainWrapper<T> from(String tableName) {
        wrapperChildren.from(tableName);
        return this;
    }

    @Override
    public LambdaSaveChainWrapper<T> setEntity(T entity) {
        wrapperChildren.setEntity(entity);
        return this;
    }

    @Override
    public LambdaSaveChainWrapper<T> addColumn(boolean condition, SFunction<T, ?> column, Object val) {
        wrapperChildren.addColumn(condition,column,val);
        return this;
    }

    @Override
    public String getSqlSet() {
        return wrapperChildren.getSqlSet();
    }
}
