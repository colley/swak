package com.swak.jdbc.query;


import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/11 17:37
 */
public class SwakColumnRowMapper<T> implements RowMapper<T> {

    private RowMapper<T> rowMapper;


    public SwakColumnRowMapper(Class<T> requiredType) {
        this.setRequiredType(requiredType);
    }

    public void setRequiredType(Class<T> requiredType) {
        boolean isPrimitive = requiredType.isPrimitive();
        if (isPrimitive) {
            rowMapper = new SingleColumnRowMapper<>(requiredType);
        } else {
            rowMapper = new BeanPropertyRowMapper<>(requiredType);
        }
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rowMapper.mapRow(rs, rowNum);
    }
}
