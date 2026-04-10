package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.conditions.update.SwakUpdate;
import com.swak.jdbc.conditions.update.UpdateWrapper;
import com.swak.jdbc.spi.SwakJdbcTemplate;

/**
 * UpdateChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class UpdateChainWrapper<T> extends AbstractChainWrapper<T, String, UpdateChainWrapper<T>, UpdateWrapper<T>>
        implements ChainUpdate<T>, SwakUpdate<UpdateChainWrapper<T>, String, T> {

    private final SwakJdbcTemplate baseMapper;

    public UpdateChainWrapper(SwakJdbcTemplate baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new UpdateWrapper<>();
    }

    public UpdateChainWrapper(SwakJdbcTemplate baseMapper, T entity) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new UpdateWrapper<>(entity);
    }

    public UpdateChainWrapper(SwakJdbcTemplate baseMapper, Class<T> entityClass) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new UpdateWrapper<>(entityClass);
    }

    @Override
    public SwakJdbcTemplate getBaseMapper() {
        return baseMapper;
    }

    @Override
    public UpdateChainWrapper<T> from(String tableName) {
        wrapperChildren.from(tableName);
        return this;
    }

    @Override
    public UpdateChainWrapper<T> set(boolean condition, String column, Object val) {
        wrapperChildren.set(condition, column, val);
        return this;
    }

    @Override
    public UpdateChainWrapper<T> setSql(boolean condition, String sql) {
        wrapperChildren.setSql(condition, sql);
        return this;
    }

    @Override
    public String getSqlSet() {
        return wrapperChildren.getSqlSet();
    }
}
