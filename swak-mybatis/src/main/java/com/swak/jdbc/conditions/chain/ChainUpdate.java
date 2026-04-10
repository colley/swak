package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.conditions.SwakWrapper;
import com.swak.jdbc.conditions.update.SwakUpdate;
import com.swak.jdbc.enums.SqlKeyword;

/**
 * ChainUpdate.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface ChainUpdate<T> extends ChainWrapper<T> {
    /**
     * 更新数据
     *
     * @return 是否成功
     */
    default boolean update() {
        int result = getBaseMapper().update(getWrapper());
        return result >= 1;
    }

    default boolean update(T entity) {
        SwakWrapper<T> wrapper = getWrapper();
        if (wrapper instanceof SwakUpdate) {
            ((SwakUpdate<?, ?, T>) wrapper).setEntity(entity);
        }
        int result = getBaseMapper().update(wrapper);
        return result >= 1;
    }

    /**
     * 删除数据
     *
     * @return 是否成功
     */
    default boolean remove() {
        SwakWrapper<T> wrapper = getWrapper();
        wrapper.setSqlKeyword(SqlKeyword.DELETE);
        int result = getBaseMapper().update(wrapper);
        return result >= 1;
    }
}
