package com.swak.jdbc.metadata;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.swak.jdbc.toolkit.TableHelper;
import lombok.Getter;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.type.UnknownTypeHandler;

import java.util.Objects;

/**
 * 缓存
 */
@Getter
public class SelectCache {

    /**
     * 实体类
     */
    private final Class<?> clazz;


    /**
     * 查询字段
     */
    private final String column;

    /**
     * 字段类型
     */
    private final Class<?> columnType;

    /**
     * 查询字段 去除特殊符号 比如反引号,单引号,双引号等
     */
    private final String tagColumn;

    /**
     * 字段属性名
     */
    private final String columProperty;

    /**
     * mp 字段信息
     */
    private final TableFieldInfo tableFieldInfo;

    /**
     * 使用使用 hasTypeHandle
     */
    private final boolean hasTypeHandle;

    /**
     * hasTypeHandle 类型
     */
    private final TypeHandler<?> typeHandler;

    public SelectCache(Class<?> clazz, String column, Class<?> columnType, String columProperty, TableFieldInfo tableFieldInfo) {
        this.clazz = clazz;
        this.column = column;
        this.columnType = columnType;
        this.columProperty = columProperty;
        this.tagColumn = com.baomidou.mybatisplus.core.toolkit.StringUtils.getTargetColumn(column);
        this.tableFieldInfo = tableFieldInfo;
        if (Objects.isNull(tableFieldInfo)) {
            this.hasTypeHandle = false;
            this.typeHandler = null;
        } else {
            this.hasTypeHandle = this.tableFieldInfo.getTypeHandler() != null && tableFieldInfo.getTypeHandler() != UnknownTypeHandler.class;
            if (this.hasTypeHandle) {
                TableInfo info = TableHelper.get(clazz);
                this.typeHandler = getTypeHandler(info.getConfiguration(), tableFieldInfo);
            } else {
                this.typeHandler = null;
            }
        }
    }


    private TypeHandler<?> getTypeHandler(Configuration configuration, TableFieldInfo info) {
        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        TypeHandler<?> typeHandler = registry.getMappingTypeHandler(info.getTypeHandler());
        if (typeHandler == null) {
            typeHandler = registry.getInstance(info.getPropertyType(), info.getTypeHandler());
        }
        return typeHandler;
    }
}
