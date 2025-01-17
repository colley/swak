package com.swak.common.enums;


import com.google.common.collect.Maps;
import com.swak.common.dto.SelectDataVo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * LocalTypeRepository.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public class LocalTypeRepository {
    private static  Map<String, Class<? extends EnumType>> typeRepository = new ConcurrentHashMap<>();
    private  static Class<? extends  EnumType> getEnumTypeClass(String enumTypeName) {
        return typeRepository.get(enumTypeName.toLowerCase());
    }
    public static List<SelectDataVo> findLocalEnumTypeCode(String enumTypeName,String category) {
        enumTypeName = enumTypeName.replaceAll("_","");
        Class<? extends  EnumType> enumTypeClass = getEnumTypeClass(enumTypeName);
        if(Objects.isNull(enumTypeClass)) {
            return Collections.emptyList();
        }
        Map<String,String> categoryMap = Maps.newHashMap();
        if(StringUtils.isNotEmpty(category)) {
            Optional.ofNullable(findLocalEnumCategory(category)).ifPresent(categoryMap::putAll);
        }
        return Arrays.stream(enumTypeClass.getEnumConstants())
                .filter(item->!item.isHidden())
                .sorted(Comparator.comparing(EnumType::order)).map(enumType-> {
                    SelectDataVo selectDataVo = new SelectDataVo();
                    selectDataVo.setSortOrder(enumType.order());
                    selectDataVo.setName(enumType.getI18nName());
                    selectDataVo.setLabel(selectDataVo.getName());
                    selectDataVo.setValue(enumType.getValue());
                    selectDataVo.setCategory(enumType.category());
                    selectDataVo.setCategoryName(categoryMap.get(enumType.category()));
                    return selectDataVo;
                }).collect(Collectors.toList());
    }

    public static List<SelectDataVo> findLocalEnumTypeCode(String enumTypeName) {
        return findLocalEnumTypeCode(enumTypeName,null);
    }

    public static Map<String,String> findLocalEnumCategory(String category) {
        if(StringUtils.isEmpty(category)) {
            return Collections.emptyMap();
        }
        category = category.replaceAll("_","");
        Class<? extends  EnumType> enumTypeClass = getEnumTypeClass(category);
        if(Objects.isNull(enumTypeClass)) {
            return Collections.emptyMap();
        }
        return Arrays.stream(enumTypeClass.getEnumConstants())
                .filter(item->!item.isHidden())
                .collect(Collectors.toMap(k->k.getValue(),v->v.getI18nName(),(v1,v2)->v1));
    }

    public static void doRegistration(String className,Class<? extends  EnumType> enumTypeClass) {
        typeRepository.put(className.toLowerCase(),enumTypeClass);
    }
}
