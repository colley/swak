package com.swak.jdbc.spi;

import com.swak.common.dto.SwakPage;
import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;
import com.swak.jdbc.conditions.SwakWrapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

/**
 * SwakJdbcTemplate.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface SwakJdbcTemplate extends SpiPriority {

    <T> List<T> selectList(SwakWrapper<T> queryWrapper);

    List<Map<String, Object>> selectListMap(SwakWrapper<?> queryWrapper);

    <T> List<T> selectList(SwakWrapper<?> queryWrapper, Class<T> elementType);

    <T> T selectForObject(SwakWrapper<?> queryWrapper, Class<T> elementType);

    <T> T selectForObject(SwakWrapper<?> queryWrapper, RowMapper<T> rowMapper);

    <T> List<T> selectList(SwakWrapper<?> queryWrapper, ResultSetExtractor<List<T>> resultSetExtractor);

    default <T> T selectOne(SwakWrapper<T> queryWrapper) {
        return selectForObject(queryWrapper, queryWrapper.getEntityClass());
    }

    /**
     * 根据 entity 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件（可以为 RowBounds.DEFAULT）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    <T, E extends SwakPage<T>> E selectPage(E page, SwakWrapper<?> queryWrapper, Class<T> elementType);

    default <T, E extends SwakPage<T>> E selectPage(E page, SwakWrapper<T> queryWrapper) {
        return selectPage(page, queryWrapper, queryWrapper.getEntityClass());
    }

    /**
     * 根据 Wrapper 条件，查询全部记录（并翻页）
     *
     * @param page         分页查询条件
     * @param queryWrapper 实体对象封装操作类
     */
    <E extends SwakPage<Map<String, Object>>> E selectMapsPage(E page, SwakWrapper<?> queryWrapper);

    int update(SwakWrapper<?> queryWrapper);


    int save(SwakWrapper<?> queryWrapper);


     static SwakJdbcTemplate getSwakJdbcTemplate() {
        return SpiServiceFactory.loadFirst(SwakJdbcTemplate.class);
    }
}
