package com.swak.autoconfigure.service.impl;

import com.google.common.collect.Maps;
import com.swak.autoconfigure.service.LocalDictionaryService;
import com.swak.common.dto.SelectDataVo;
import com.swak.common.enums.LocalTypeRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class LocalDictionaryServiceImpl implements LocalDictionaryService {

    @Override
    public List<SelectDataVo> getLocalDictCode(String categoryType, String relCategory) {
        //过滤掉name是空的
        return Optional.ofNullable(LocalTypeRepository.findLocalEnumTypeCode(categoryType, relCategory))
                .orElse(Collections.emptyList()).stream().filter(item -> StringUtils.isNotEmpty(item.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Collection<SelectDataVo>> getBathDictCode(Set<String> categoryTypes) {
        Map<String, Collection<SelectDataVo>> result = Maps.newHashMap();
        categoryTypes.forEach(localCategory -> {
            List<SelectDataVo> localCodeList = getLocalDictCode(localCategory, null);
            result.put(localCategory, localCodeList);
        });
        return result;
    }
}
