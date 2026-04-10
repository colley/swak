package com.swak.jdbc.metadata;

import com.swak.common.util.ClassUtils;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.ReflectionKit;
import com.swak.jdbc.annotation.FieldStrategy;
import com.swak.jdbc.annotation.SColumn;
import com.swak.jdbc.annotation.STable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

/**
 * TableInfoHelper.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class TableInfoHelper {


    /**
     * 储存反射类表信息
     */
    private static final Map<Class<?>, TableInfo> TABLE_INFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 储存表名对应的反射类表信息
     */
    private static final Map<String,TableInfo> TABLE_NAME_INFO_CACHE = new ConcurrentHashMap<>();


    /**
     * <p>
     * 获取实体映射表信息
     * </p>
     *
     * @param clazz 反射实体类
     * @return 数据库表反射信息
     */
    public static TableInfo getTableInfo(Class<?> clazz) {
        if (clazz == null || ClassUtils.isPrimitive(clazz) || clazz == String.class || clazz.isInterface()) {
            return null;
        }
        Class<?> targetClass = ClassUtils.getUserClass(clazz);
        TableInfo tableInfo = TABLE_INFO_CACHE.get(targetClass);
        if (null != tableInfo) {
            return tableInfo;
        }
        //尝试获取父类缓存
        Class<?> currentClass = clazz;
        while (null == tableInfo && Object.class != currentClass) {
            currentClass = currentClass.getSuperclass();
            tableInfo = TABLE_INFO_CACHE.get(ClassUtils.getUserClass(currentClass));
        }
        //把父类的移到子类中来
        if (tableInfo != null) {
            TABLE_INFO_CACHE.put(targetClass, tableInfo);
        }
        if(Objects.isNull(tableInfo)){
            tableInfo = initTableInfo(clazz);
        }
        return tableInfo;
    }

    /**
     * <p>
     * 根据表名获取实体映射表信息
     * </p>
     *
     * @param tableName 表名
     * @return 数据库表反射信息
     */
    public static TableInfo getTableInfo(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return null;
        }
        return TABLE_NAME_INFO_CACHE.get(tableName);
    }

    /**
     * <p>
     * 获取所有实体映射表信息
     * </p>
     *
     * @return 数据库表反射信息集合
     */
    public static List<TableInfo> getTableInfos() {
        return Collections.unmodifiableList(new ArrayList<>(TABLE_INFO_CACHE.values()));
    }

    /**
     * <p>
     * 实体类反射获取表信息【初始化】
     * </p>
     *
     * @param clazz 反射实体类
     * @return 数据库表反射信息
     */
    public synchronized static TableInfo initTableInfo(Class<?> clazz) {
        TableInfo targetTableInfo = TABLE_INFO_CACHE.get(clazz);
        if (targetTableInfo != null) {
            return targetTableInfo;
        }
        /* 没有获取到缓存信息,则初始化 */
        TableInfo tableInfo = new TableInfo(clazz);
        /* 初始化表名相关 */
        initTableName(clazz,tableInfo);
        /* 初始化字段相关 */
        initTableFields(clazz, tableInfo);

        TABLE_INFO_CACHE.put(clazz, tableInfo);
        TABLE_NAME_INFO_CACHE.put(tableInfo.getTableName(), tableInfo);
        return tableInfo;
    }

    /**
     * 初始化 表数据库类型,表名
     */
    private static void initTableName(Class<?> clazz,TableInfo tableInfo) {
        /* 数据库全局配置 */
        STable table = clazz.getAnnotation(STable.class);
        String tableName = clazz.getSimpleName();
        if (table != null) {
            if (StringUtils.isNotBlank(table.value())) {
                tableName = table.value();
            } else {
                tableName = initTableNameWithDbConfig(tableName);
            }
        } else {
            tableName = initTableNameWithDbConfig(tableName);
        }
        tableInfo.setTableName(tableName);
    }

    private static String initTableNameWithDbConfig(String className) {
        String tableName = className;
        tableName = GetterUtil.camelToUnderline(tableName);
        tableName = GetterUtil.firstToLowerCase(tableName);
        return tableName;
    }

    /**
     * 初始化 表主键,表字段
     * @param clazz        实体类
     * @param tableInfo    数据库表反射信息
     */
    private static void initTableFields(Class<?> clazz, TableInfo tableInfo) {
        /* 数据库全局配置 */
        List<Field> list = getAllFields(clazz);
        List<TableFieldInfo> fieldList = new ArrayList<>(list.size());
        for (Field field : list) {
            final SColumn tableField = field.getAnnotation(SColumn.class);
            /* 有 @TableField 注解的字段初始化 */
            if (tableField != null) {
                TableFieldInfo tableFieldInfo = new TableFieldInfo()
                        .setField(field).setColumn(tableField.value()).setProperty(field.getName())
                        .setColumnType(field.getType())
                        .setInsertStrategy(tableField.insertStrategy())
                        .setUpdateStrategy(tableField.updateStrategy());
                fieldList.add(tableFieldInfo);
                continue;
            }
            /* 无 @TableField 注解的字段初始化 */
            TableFieldInfo tableFieldInfo = new TableFieldInfo()
                    .setField(field).setColumn(GetterUtil.camelToUnderline(field.getName())).setProperty(field.getName())
                    .setColumnType(field.getType())
                    .setInsertStrategy(FieldStrategy.DEFAULT)
                    .setUpdateStrategy(FieldStrategy.DEFAULT);
            fieldList.add(tableFieldInfo);
        }
        /* 字段列表 */
        tableInfo.setFieldList(fieldList);
    }


    /**
     * <p>
     * 获取该类的所有属性列表
     * </p>
     *
     * @param clazz 反射类
     * @return 属性集合
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fieldList = ReflectionKit.getFieldList(ClassUtils.getUserClass(clazz));
        return fieldList.stream()
                .filter(field -> {
                    /* 过滤注解非表字段属性 */
                    SColumn tableField = field.getAnnotation(SColumn.class);
                    return (tableField == null || tableField.exist());
                }).collect(toList());
    }
}
