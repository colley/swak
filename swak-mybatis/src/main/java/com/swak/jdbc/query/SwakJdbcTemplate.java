package com.swak.jdbc.query;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.swak.common.dto.SwakPage;
import com.swak.common.util.DateTimeUtils;
import com.swak.jdbc.conditions.SwakWrapper;
import com.swak.jdbc.dialects.Dialect;
import com.swak.jdbc.dialects.DialectModel;
import com.swak.jdbc.dialects.DialectRegistry;
import com.swak.jdbc.parser.CountBoundSql;
import com.swak.jdbc.parser.StaticBoundSql;
import com.swak.jdbc.parser.SwakBoundSql;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.*;


/**
 * SwakJdbcTemplate
 *
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/7 09:43
 **/
@Slf4j
public class SwakJdbcTemplate extends JdbcTemplate {

    private Dialect dialect;

    public SwakJdbcTemplate() {
        super();
    }

    public SwakJdbcTemplate(DataSource dataSource) {
        this.setDataSource(dataSource);
        this.afterPropertiesSet();
    }

    public SwakJdbcTemplate(DataSource dataSource, boolean lazyInit) {
        this.setDataSource(dataSource);
        this.setLazyInit(lazyInit);
        this.afterPropertiesSet();
    }

    public <T> List<T> selectList(SwakWrapper<T> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<T> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = super.query(newSql,new SwakColumnRowMapper<>(queryWrapper.getEntityClass()));
            } else {
                result = super.query(newSql,new SwakColumnRowMapper<>(queryWrapper.getEntityClass()), parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return result;
    }

    public List<Map<String,Object>> selectListMap(SwakWrapper<?> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<Map<String, Object>> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = super.queryForList(newSql);
            } else {
                result = super.queryForList(newSql, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return result;
    }

    public <T> List<T> selectList(SwakWrapper<?> queryWrapper, Class<T> elementType) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<T> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = super.query(newSql, new SwakColumnRowMapper(elementType));
            } else {
                result = super.query(newSql, new SwakColumnRowMapper(elementType), parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return result;
    }

    public <T> T selectForObject(SwakWrapper<?> queryWrapper, Class<T> elementType) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        T result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = super.queryForObject(newSql, new SwakColumnRowMapper<>(elementType));
            } else {
                result = super.queryForObject(newSql, new SwakColumnRowMapper<>(elementType), parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return result;
    }

    public <T> T selectForObject(SwakWrapper<?> queryWrapper, RowMapper<T> rowMapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        T result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = super.queryForObject(newSql, rowMapper);
            } else {
                result = super.queryForObject(newSql, rowMapper, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return result;
    }

    public <T> List<T> selectList(SwakWrapper<?> queryWrapper, ResultSetExtractor<List<T>> resultSetExtractor) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql boundSql = queryWrapper.getBoundSql();
        String newSql = boundSql.getSql();
        Object[] parameterObjectValues = boundSql.getParamObjectValues();
        List<T> result;
        try {
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                result = super.query(newSql, resultSetExtractor);
            } else {
                result = super.query(newSql, resultSetExtractor, parameterObjectValues);
            }
        } finally {
            logRunTime(newSql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws IllegalArgumentException{
        super.afterPropertiesSet();
        if(Objects.isNull(this.dialect)){
            this.dialect = DialectRegistry.getDialect(super.getDataSource());
        }
    }

    /**
     * 根据 entity 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件（可以为 RowBounds.DEFAULT）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
   public <T, E extends SwakPage<T>>  E selectPage(E page, SwakWrapper<?> queryWrapper, Class<T> elementType) {
       long startTime = System.currentTimeMillis();
       SwakBoundSql selBoundSql = queryWrapper.getBoundSql();
       CountBoundSql countBoundSql = new CountBoundSql(page,selBoundSql);
       Object[] parameterObjectValues = selBoundSql.getParamObjectValues();
       String countSql = countBoundSql.getSql();
       Long total = super.queryForObject(countSql,Long.class,parameterObjectValues);
       logRunTime(countBoundSql.getSql(), startTime, parameterObjectValues != null ?
               JSON.toJSONString(parameterObjectValues) : "[]");
       page.setTotal(total == null ? 0L : total);
       if(page.getTotal()<=0) {
           return page;
       }
       DialectModel dialectModel = dialect.paginationSql(selBoundSql.getSql(), page.offset(), page.getSize());
       dialectModel.consumers(selBoundSql.getParameterMapping(),selBoundSql.getAdditionalParameters());
       SwakBoundSql queryBoundSql = new StaticBoundSql(dialectModel.getDialectSql(),selBoundSql.getParameterMapping(),selBoundSql.getAdditionalParameters());
       List<T> records;
       String querySql = queryBoundSql.getSql();
       try {
           parameterObjectValues = queryBoundSql.getParamObjectValues();
           if (ArrayUtils.isEmpty(parameterObjectValues)) {
               records = super.query(querySql, new SwakColumnRowMapper<>(elementType));
           } else {
               records = super.query(querySql, new SwakColumnRowMapper<>(elementType), parameterObjectValues);
           }
           page.setRecords(records);
       } finally {
           logRunTime(querySql, startTime, parameterObjectValues != null ?
                   JSON.toJSONString(parameterObjectValues) : "[]");
       }
       return page;
   }

    public <T, E extends SwakPage<T>>  E selectPage(E page, SwakWrapper<T> queryWrapper) {
        return selectPage(page,queryWrapper,queryWrapper.getEntityClass());
    }

    /**
     * 根据 Wrapper 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件
     * @param queryWrapper 实体对象封装操作类
     */
    public  <E extends SwakPage<Map<String, Object>>> E selectMapsPage(E page, SwakWrapper<?> queryWrapper) {
        long startTime = System.currentTimeMillis();
        SwakBoundSql selBoundSql = queryWrapper.getBoundSql();
        CountBoundSql countBoundSql = new CountBoundSql(page,selBoundSql);
        Object[] parameterObjectValues = selBoundSql.getParamObjectValues();
        String countSql = countBoundSql.getSql();
        Long total = super.queryForObject(countSql,Long.class,parameterObjectValues);
        logRunTime(countBoundSql.getSql(), startTime, parameterObjectValues != null ?
                JSON.toJSONString(parameterObjectValues) : "[]");
        page.setTotal(total == null ? 0L : total);
        if(page.getTotal()<=0) {
            return page;
        }
        DialectModel dialectModel = dialect.paginationSql(selBoundSql.getSql(), page.offset(), page.getSize());
        dialectModel.consumers(selBoundSql.getParameterMapping(),selBoundSql.getAdditionalParameters());
        SwakBoundSql queryBoundSql = new StaticBoundSql(dialectModel.getDialectSql(),selBoundSql.getParameterMapping(),selBoundSql.getAdditionalParameters());
        List<Map<String, Object>> records;
        String querySql = queryBoundSql.getSql();
        try {
            parameterObjectValues = queryBoundSql.getParamObjectValues();
            if (ArrayUtils.isEmpty(parameterObjectValues)) {
                records = super.queryForList(querySql);
            } else {
                records = super.queryForList(querySql, parameterObjectValues);
            }
            page.setRecords(records);
        } finally {
            logRunTime(querySql, startTime, parameterObjectValues != null ?
                    JSON.toJSONString(parameterObjectValues) : "[]");
        }
        return page;
    }

    private void logRunTime(String sql, Long start, String params) {
        long elapsed = System.currentTimeMillis() - start;
        String result = StringUtils.isNotBlank(sql) ? " Consume Time：" + elapsed + " ms " +
                DateTimeUtils.date2String(new Date()) +
                "\n Execute SQL：" + sql.replaceAll("[\\s]+", " ") + "\n" : "" +
                "\n Execute params : " + params;
        logger.info(result);
    }
}
