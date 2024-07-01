package com.swak.jdbc.common;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.swak.common.exception.SwakException;


public class TableAssert {

    public static void hasTable(TableInfo tableInfo, Class<?> entityClass) {
        if (tableInfo == null) {
            throw new SwakException(String.format(
                    "mapper not find by class <%s> , add mapper and extends BaseMapper<T>",
                    entityClass.getSimpleName()));
        }
    }
}
