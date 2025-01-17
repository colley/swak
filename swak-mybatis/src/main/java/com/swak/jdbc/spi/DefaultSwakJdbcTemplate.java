package com.swak.jdbc.spi;

import com.alibaba.fastjson2.JSON;
import com.swak.common.dto.SwakPage;
import com.swak.common.util.DateTimeUtils;
import com.swak.jdbc.common.SwakColumnRowMapper;
import com.swak.jdbc.conditions.SwakWrapper;
import com.swak.jdbc.dialects.Dialect;
import com.swak.jdbc.dialects.DialectModel;
import com.swak.jdbc.dialects.DialectRegistry;
import com.swak.jdbc.parser.CountBoundSql;
import com.swak.jdbc.parser.StaticBoundSql;
import com.swak.jdbc.parser.SwakBoundSql;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * SwakJdbcTemplate.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Slf4j
public class DefaultSwakJdbcTemplate implements SwakJdbcTemplate, InitializingBean {

    private Dialect dialect;

    private JdbcTemplate jdbcTemplate;

    public DefaultSwakJdbcTemplate() {
        super();
    }

    public DefaultSwakJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.afterPropertiesSet();
    }

    public DefaultSwakJdbcTemplate(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public DefaultSwakJdbcTemplate(DataSource dataSource, boolean lazyInit) {
        this(new JdbcTemplate(dataSource,lazyInit));
    }

    @Override
    public <T> List<T> selectList(SwakWrapper<T> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<T> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = jdbcTemplate.query(newSql, new SwakColumnRowMapper<>(queryWrapper.getEntityClass()));
            } else {
                result = jdbcTemplate.query(newSql, new SwakColumnRowMapper<>(queryWrapper.getEntityClass()), parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> selectListMap(SwakWrapper<?> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<Map<String, Object>> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = jdbcTemplate.queryForList(newSql);
            } else {
                result = jdbcTemplate.queryForList(newSql, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return result;
    }

    @Override
    public <T> List<T> selectList(SwakWrapper<?> queryWrapper, Class<T> elementType) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<T> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = jdbcTemplate.query(newSql, new SwakColumnRowMapper<>(elementType));
            } else {
                result = jdbcTemplate.query(newSql, new SwakColumnRowMapper<>(elementType), parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return result;
    }

    @Override
    public <T> T selectForObject(SwakWrapper<?> queryWrapper, Class<T> elementType) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        T result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = jdbcTemplate.queryForObject(newSql, new SwakColumnRowMapper<>(elementType));
            } else {
                result = jdbcTemplate.queryForObject(newSql, new SwakColumnRowMapper<>(elementType), parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return result;
    }

    @Override
    public <T> T selectForObject(SwakWrapper<?> queryWrapper, RowMapper<T> rowMapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        T result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = jdbcTemplate.queryForObject(newSql, rowMapper);
            } else {
                result = jdbcTemplate.queryForObject(newSql, rowMapper, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return result;
    }

    @Override
    public <T> List<T> selectList(SwakWrapper<?> queryWrapper, ResultSetExtractor<List<T>> resultSetExtractor) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<T> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = jdbcTemplate.query(newSql, resultSetExtractor);
            } else {
                result = jdbcTemplate.query(newSql, resultSetExtractor, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return result;
    }


    @Override
    public void afterPropertiesSet() throws IllegalArgumentException {
        if (Objects.isNull(this.dialect)) {
            this.dialect = DialectRegistry.getDialect(jdbcTemplate.getDataSource());
        }
    }

    /**
     * 根据 entity 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件（可以为 RowBounds.DEFAULT）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    @Override
    public <T, E extends SwakPage<T>> E selectPage(E page, SwakWrapper<?> queryWrapper, Class<T> elementType) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql selBoundSql = queryWrapper.getBoundSql();
        CountBoundSql countBoundSql = new CountBoundSql(page, selBoundSql);
        Object[] parameterObjectValues = selBoundSql.getParamObjectValues();
        String countSql = countBoundSql.getSql();
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, parameterObjectValues);
        logRunTime(countBoundSql.getSql(), startTime, parameterObjectValues);
        page.setTotal(total == null ? 0L : total);
        if (page.getTotal() <= 0) {
            return page;
        }
        DialectModel dialectModel = dialect.paginationSql(selBoundSql.getSql(), page.offset(), page.getSize());
        dialectModel.consumers(selBoundSql.getParameterMapping(), selBoundSql.getAdditionalParameters());
        SwakBoundSql queryBoundSql = new StaticBoundSql(dialectModel.getDialectSql(), selBoundSql.getParameterMapping(), selBoundSql.getAdditionalParameters());
        List<T> records;
        String querySql = queryBoundSql.getSql();
        try {
            parameterObjectValues = queryBoundSql.getParamObjectValues();
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                records = jdbcTemplate.query(querySql, new SwakColumnRowMapper<>(elementType));
            } else {
                records = jdbcTemplate.query(querySql, new SwakColumnRowMapper<>(elementType), parameterObjectValues);
            }
            page.setRecords(records);
        } finally {
            logRunTime(querySql, startTime, parameterObjectValues);
        }
        return page;
    }

    /**
     * 根据 Wrapper 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件
     * @param queryWrapper 实体对象封装操作类
     */
    @Override
    public <E extends SwakPage<Map<String, Object>>> E selectMapsPage(E page, SwakWrapper<?> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql selBoundSql = queryWrapper.getBoundSql();
        CountBoundSql countBoundSql = new CountBoundSql(page, selBoundSql);
        Object[] parameterObjectValues = selBoundSql.getParamObjectValues();
        String countSql = countBoundSql.getSql();
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, parameterObjectValues);
        logRunTime(countBoundSql.getSql(), startTime, parameterObjectValues);
        page.setTotal(total == null ? 0L : total);
        if (page.getTotal() <= 0) {
            return page;
        }
        DialectModel dialectModel = dialect.paginationSql(selBoundSql.getSql(), page.offset(), page.getSize());
        dialectModel.consumers(selBoundSql.getParameterMapping(), selBoundSql.getAdditionalParameters());
        SwakBoundSql queryBoundSql = new StaticBoundSql(dialectModel.getDialectSql(), selBoundSql.getParameterMapping(), selBoundSql.getAdditionalParameters());
        List<Map<String, Object>> records;
        String querySql = queryBoundSql.getSql();
        try {
            parameterObjectValues = queryBoundSql.getParamObjectValues();
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                records = jdbcTemplate.queryForList(querySql);
            } else {
                records = jdbcTemplate.queryForList(querySql, parameterObjectValues);
            }
            page.setRecords(records);
        } finally {
            logRunTime(querySql, startTime, parameterObjectValues);
        }
        return page;
    }
    @Override
    public int update(SwakWrapper<?> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        int rows = 0;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                rows = jdbcTemplate.update(newSql);
            } else {
                rows = jdbcTemplate.update(newSql, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return rows;
    }



    @Override
    public int save(SwakWrapper<?> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        int rows = 0;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                rows = jdbcTemplate.update(newSql);
            } else {
                rows = jdbcTemplate.update(newSql, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues);
        }
        return rows;
    }

    private void logRunTime(String sql, Long start, Object[] params) {
        long elapsed = System.currentTimeMillis() - start;
        Object[] args = new Object[]{elapsed, DateTimeUtils.date2String(new Date()), sql.replaceAll("[\\s]+", " "),
                (params != null ? JSON.toJSONString(params) : "[]")};
        log.info(" \n Consume Time：{} ms at {} \n Execute SQL：{} \n Execute params :{}", args);
    }


}
