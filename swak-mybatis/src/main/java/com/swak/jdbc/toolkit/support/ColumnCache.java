package com.swak.jdbc.toolkit.support;

import com.swak.jdbc.metadata.TableInfo;
import com.swak.jdbc.common.TableAssert;
import com.swak.jdbc.metadata.SelectCache;
import com.swak.jdbc.toolkit.FieldStringMap;
import com.swak.jdbc.toolkit.TableHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * select缓存
 * @author colley.ma
 * @version v1.0
 * @since 2024/3/6 15:25
 **/
public class ColumnCache {

    private static final Map<Class<?>, List<SelectCache>> LIST_CACHE = new ConcurrentHashMap<>();

    private static final Map<Class<?>, FieldStringMap<SelectCache>> MAP_CACHE = new ConcurrentHashMap<>();

    public static List<SelectCache> getListField(Class<?> clazz) {
        return LIST_CACHE.computeIfAbsent(clazz, c -> {
            TableInfo tableInfo = TableHelper.get(clazz);
            TableAssert.hasTable(tableInfo,clazz);
            List<SelectCache> list = new ArrayList<>();
            list.addAll(tableInfo.getFieldList().stream().map(f -> new SelectCache(clazz, f.getColumn(),
                    f.getColumnType(), f.getProperty(), f)).collect(Collectors.toList()));
            return list;
        });
    }

    public static Map<String, SelectCache> getMapField(Class<?> clazz) {
        return MAP_CACHE.computeIfAbsent(clazz, c -> getListField(c).stream().collect(Collectors.toMap(
                i -> i.getColumProperty().toUpperCase(Locale.ENGLISH),
                Function.identity(), (i, j) -> j, FieldStringMap::new)));
    }
}
