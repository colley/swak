package com.swak.jdbc.conditions.chain;


import com.swak.jdbc.conditions.update.LambdaUpdateWrapper;
import com.swak.jdbc.conditions.update.SwakUpdate;
import com.swak.jdbc.metadata.SFunction;
import com.swak.jdbc.spi.SwakJdbcTemplate;

/**
 * LambdaUpdateChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class LambdaUpdateChainWrapper<T> extends AbstractChainWrapper<T, String, LambdaUpdateChainWrapper<T>, LambdaUpdateWrapper<T>>
        implements ChainUpdate<T>, SwakUpdate<LambdaUpdateChainWrapper<T>, SFunction<T, ?>, T> {

    private final SwakJdbcTemplate baseMapper;

    public LambdaUpdateChainWrapper(SwakJdbcTemplate baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaUpdateWrapper<>();
    }

    public LambdaUpdateChainWrapper(SwakJdbcTemplate baseMapper, T entity) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaUpdateWrapper<>(entity);
    }

    public LambdaUpdateChainWrapper(SwakJdbcTemplate baseMapper, Class<T> entityClass) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new LambdaUpdateWrapper<>(entityClass);
    }

    @Override
    public SwakJdbcTemplate getBaseMapper() {
        return baseMapper;
    }

    @Override
    public LambdaUpdateChainWrapper<T> from(String tableName) {
        wrapperChildren.from(tableName);
        return this;
    }

    @Override
    public LambdaUpdateChainWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        wrapperChildren.set(condition, column, val);
        return this;
    }

    @Override
    public LambdaUpdateChainWrapper<T> setSql(boolean condition, String sql) {
        wrapperChildren.setSql(condition, sql);
        return this;
    }

    @Override
    public String getSqlSet() {
        return wrapperChildren.getSqlSet();
    }
}
