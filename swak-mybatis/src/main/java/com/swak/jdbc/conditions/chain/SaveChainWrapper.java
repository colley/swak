package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.conditions.update.SaveWrapper;
import com.swak.jdbc.conditions.update.SwakSave;
import com.swak.jdbc.spi.SwakJdbcTemplate;

import java.util.Map;

/**
 * SaveChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class SaveChainWrapper<T> extends AbstractChainWrapper<T, String, SaveChainWrapper<T>, SaveWrapper<T>>
        implements ChainSave<T>, SwakSave<SaveChainWrapper<T>, String, T> {

    private final SwakJdbcTemplate baseMapper;

    public SaveChainWrapper(SwakJdbcTemplate baseMapper) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new SaveWrapper<>();
    }

    public SaveChainWrapper(SwakJdbcTemplate baseMapper, T entity) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new SaveWrapper<>(entity);
    }

    public SaveChainWrapper(SwakJdbcTemplate baseMapper, Class<T> entityClass) {
        super();
        this.baseMapper = baseMapper;
        super.wrapperChildren = new SaveWrapper<>(entityClass);
    }

    @Override
    public SwakJdbcTemplate getBaseMapper() {
        return baseMapper;
    }

    @Override
    public SaveWrapper<T> getWrapper() {
        return wrapperChildren;
    }

    @Override
    public SaveChainWrapper<T> addColumn(String... columns) {
         wrapperChildren.addColumn(columns);
         return this;
    }

    @Override
    public SaveChainWrapper<T> addValue(String column, Object value) {
        wrapperChildren.addValue(column,value);
        return this;
    }

    @Override
    public SaveChainWrapper<T> addColumn(Map<String, Object> columnValue) {
        wrapperChildren.addColumn(columnValue);
        return this;
    }

    @Override
    public SaveChainWrapper<T> from(String tableName) {
        wrapperChildren.from(tableName);
        return this;
    }

    @Override
    public SaveChainWrapper<T> setEntity(T entity) {
        wrapperChildren.setEntity(entity);
        return this;
    }

    @Override
    public SaveChainWrapper<T> addColumn(boolean condition, String column, Object val) {
        wrapperChildren.addColumn(condition,column,val);
        return this;
    }

    @Override
    public String getSqlSet() {
        return wrapperChildren.getSqlSet();
    }
}
