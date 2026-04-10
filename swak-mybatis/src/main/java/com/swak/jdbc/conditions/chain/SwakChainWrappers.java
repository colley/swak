package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.spi.SwakJdbcTemplate;

/**
 * ChainWrappers.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public final class SwakChainWrappers {

    private SwakChainWrappers() {
        // ignore
    }

    /**
     * 链式查询 普通
     *
     * @return QueryWrapper 的包装类
     */
    public static <T> QueryChainWrapper<T> query() {
        return new QueryChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> QueryChainWrapper<T> query(T entity) {
        return new QueryChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
    }

    public static <T> LambdaChainWrapper<T> lambdaQuery() {
        return new LambdaChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> LambdaChainWrapper<T> lambdaQuery(T entity) {
        return new LambdaChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
    }

    public static <T> LambdaChainWrapper<T> lambdaQuery(Class<T> entityClass) {
        return new LambdaChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate(), entityClass);
    }


    public static <T> UpdateChainWrapper<T> update() {
        return new UpdateChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> UpdateChainWrapper<T> update(T entity) {
        return new UpdateChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
    }


    /**
     * 链式更改 lambda 式
     */
    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return new LambdaUpdateChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(T entity) {
        return lambdaUpdate((T) entity.getClass());
    }

    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(Class<T> entityClass) {
        return new LambdaUpdateChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate(), entityClass);
    }


    public static <T> SaveChainWrapper<T> save() {
        return new SaveChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> SaveChainWrapper save(T entity) {
        SaveChainWrapper<T> saveChainWrapper = save();
        saveChainWrapper.setEntity(entity);
        return saveChainWrapper;
    }

    public static <T> LambdaSaveChainWrapper lambdaSave() {
        return new LambdaSaveChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> LambdaSaveChainWrapper<T> lambdaSave(T entity) {
        LambdaSaveChainWrapper saveChainWrapper = lambdaSave();
        saveChainWrapper.setEntity(entity);
        return saveChainWrapper;
    }

    public static <T> LambdaSaveChainWrapper<T> lambdaSave(Class<T> entityClass) {
        return new LambdaSaveChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate(), entityClass);
    }
}
