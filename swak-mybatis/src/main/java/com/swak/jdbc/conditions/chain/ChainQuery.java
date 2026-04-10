package com.swak.jdbc.conditions.chain;

import com.swak.common.dto.SwakPage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ChainQuery.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface ChainQuery<T> extends ChainWrapper<T> {

    /**
     * 获取集合
     */
    default List<T> list() {
        return getBaseMapper().selectList(getWrapper());
    }

    default List<T> list(Class<T> elementType) {
        return getBaseMapper().selectList(getWrapper(), elementType);
    }

    default List<Map<String, Object>> listMap() {
        return getBaseMapper().selectListMap(getWrapper());
    }

    /**
     * 获取单个
     */
    default T one() {
        return getBaseMapper().selectOne(getWrapper());
    }

    /**
     * 获取单个
     */
    default Optional<T> oneOpt() {
        return Optional.ofNullable(one());
    }


    default <E extends SwakPage<T>> E page(E page) {
        return getBaseMapper().selectPage(page, getWrapper());
    }
}
