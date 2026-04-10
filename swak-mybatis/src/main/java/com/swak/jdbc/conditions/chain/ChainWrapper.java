package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.conditions.SwakWrapper;
import com.swak.jdbc.spi.SwakJdbcTemplate;

/**
 * ChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface ChainWrapper<T> {
    /**
     * 获取 BaseMapper
     *
     * @return BaseMapper
     */
    SwakJdbcTemplate getBaseMapper();
    /**
     * 获取最终拿去执行的 Wrapper
     *
     * @return Wrapper
     */
     SwakWrapper<T> getWrapper();
}
