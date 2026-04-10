package com.swak.jdbc.conditions.chain;

import com.swak.jdbc.conditions.SwakWrapper;
import com.swak.jdbc.conditions.update.SwakSave;

/**
 * ChainWrapper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface ChainSave<T> extends ChainWrapper<T> {
    /**
     * 保存
     * @return 是否成功
     */
    default boolean save() {
        int result = getBaseMapper().save(getWrapper());
        return result >= 1;
    }

    default boolean save(T entity) {
        SwakWrapper<T> wrapper = getWrapper();
        if(wrapper instanceof SwakSave){
            ((SwakSave<?, ?, T>) wrapper).setEntity(entity);
        }
        int result = getBaseMapper().save(wrapper);
        return result >= 1;
    }
}
