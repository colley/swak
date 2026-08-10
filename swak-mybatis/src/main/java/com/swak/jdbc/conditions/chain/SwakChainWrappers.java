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
        return query(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

	public static <T> QueryChainWrapper<T> query(SwakJdbcTemplate swakJdbcTemplate) {
		return new QueryChainWrapper(swakJdbcTemplate);
	}

	public static <T> QueryChainWrapper<T> query(SwakJdbcTemplate swakJdbcTemplate,T entity) {
		return new QueryChainWrapper(swakJdbcTemplate, entity);
	}

	public static <T> QueryChainWrapper<T> query(T entity) {
        return query(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
    }

	public static <T> LambdaChainWrapper<T> lambdaQuery(SwakJdbcTemplate swakJdbcTemplate) {
		return new LambdaChainWrapper(SwakJdbcTemplate.getSwakJdbcTemplate());
	}
    public static <T> LambdaChainWrapper<T> lambdaQuery() {
        return lambdaQuery(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

    public static <T> LambdaChainWrapper lambdaQuery(SwakJdbcTemplate swakJdbcTemplate, T entity) {
        return new LambdaChainWrapper(swakJdbcTemplate, entity);
    }

	public static <T> LambdaChainWrapper<T> lambdaQuery(T entity) {
		return lambdaQuery(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
	}

    public static <T> LambdaChainWrapper<T> lambdaQuery(SwakJdbcTemplate swakJdbcTemplate,Class<T> entityClass) {
        return new LambdaChainWrapper(swakJdbcTemplate, entityClass);
    }
	public static <T> LambdaChainWrapper<T> lambdaQuery(Class<T> entityClass) {
		return lambdaQuery(SwakJdbcTemplate.getSwakJdbcTemplate(), entityClass);
	}


    public static <T> UpdateChainWrapper<T> update(SwakJdbcTemplate swakJdbcTemplate) {
        return new UpdateChainWrapper(swakJdbcTemplate);
    }

	public static <T> UpdateChainWrapper<T> update() {
		return update(SwakJdbcTemplate.getSwakJdbcTemplate());
	}

    public static <T> UpdateChainWrapper<T> update(T entity) {
        return update(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
    }

	public static <T> UpdateChainWrapper<T> update(SwakJdbcTemplate swakJdbcTemplate,T entity) {
		return new UpdateChainWrapper(swakJdbcTemplate, entity);
	}

    /**
     * 链式更改 lambda 式
     */
    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate() {
        return lambdaUpdate(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

	public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(SwakJdbcTemplate swakJdbcTemplate) {
		return new LambdaUpdateChainWrapper(swakJdbcTemplate);
	}

    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(T entity) {
        return lambdaUpdate(SwakJdbcTemplate.getSwakJdbcTemplate(),entity);
    }

	public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(SwakJdbcTemplate swakJdbcTemplate,T entity) {
		return new LambdaUpdateChainWrapper(swakJdbcTemplate,entity);
	}

    public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(Class<T> entityClass) {
        return lambdaUpdate(SwakJdbcTemplate.getSwakJdbcTemplate(), entityClass);
    }

	public static <T> LambdaUpdateChainWrapper<T> lambdaUpdate(SwakJdbcTemplate swakJdbcTemplate,Class<T> entityClass) {
		return new LambdaUpdateChainWrapper<>(swakJdbcTemplate, entityClass);
	}


    public static <T> SaveChainWrapper<T> save() {
        return save(SwakJdbcTemplate.getSwakJdbcTemplate());
    }

	public static <T> SaveChainWrapper<T> save(SwakJdbcTemplate swakJdbcTemplate) {
		return new SaveChainWrapper(swakJdbcTemplate);
	}

    public static <T> SaveChainWrapper save(SwakJdbcTemplate swakJdbcTemplate,T entity) {
		return new SaveChainWrapper(swakJdbcTemplate, entity);
    }

	public static <T> SaveChainWrapper save(T entity) {
		return save(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
	}

    public static <T> LambdaSaveChainWrapper lambdaSave(SwakJdbcTemplate swakJdbcTemplate) {
        return new LambdaSaveChainWrapper(swakJdbcTemplate);
    }

	public static <T> LambdaSaveChainWrapper lambdaSave() {
		return lambdaSave(SwakJdbcTemplate.getSwakJdbcTemplate());
	}

    public static <T> LambdaSaveChainWrapper<T> lambdaSave(SwakJdbcTemplate swakJdbcTemplate,T entity) {
        LambdaSaveChainWrapper saveChainWrapper = lambdaSave(swakJdbcTemplate);
        saveChainWrapper.setEntity(entity);
        return saveChainWrapper;
    }

	public static <T> LambdaSaveChainWrapper<T> lambdaSave(T entity) {
		return lambdaSave(SwakJdbcTemplate.getSwakJdbcTemplate(), entity);
	}

    public static <T> LambdaSaveChainWrapper<T> lambdaSave(Class<T> entityClass) {
        return lambdaSave(SwakJdbcTemplate.getSwakJdbcTemplate(), entityClass);
    }

	public static <T> LambdaSaveChainWrapper<T> lambdaSave(SwakJdbcTemplate swakJdbcTemplate,Class<T> entityClass) {
		return new LambdaSaveChainWrapper(swakJdbcTemplate, entityClass);
	}
}
