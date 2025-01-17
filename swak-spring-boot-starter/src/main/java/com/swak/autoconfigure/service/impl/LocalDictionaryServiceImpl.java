package com.swak.autoconfigure.service.impl;

import com.google.common.collect.Maps;
import com.swak.autoconfigure.handle.DictionaryHandlerFactory;
import com.swak.autoconfigure.service.LocalDictionaryService;
import com.swak.common.dto.SelectDataVo;
import com.swak.common.enums.LocalTypeRepository;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class LocalDictionaryServiceImpl implements LocalDictionaryService {

    @Resource
    private DictionaryHandlerFactory dictionaryHandlerFactory;

    @Override
    public List<SelectDataVo> getLocalDictCode(String categoryType, String relCategory) {
        //过滤掉name是空的
        List<SelectDataVo> sources = Optional.ofNullable(LocalTypeRepository.findLocalEnumTypeCode(categoryType, relCategory))
                .orElse(Collections.emptyList()).stream().filter(item -> StringUtils.isNotEmpty(item.getName()))
                .collect(Collectors.toList());
        return dictionaryHandlerFactory.commence(sources, categoryType);
    }

    @Override
    public Map<String, Collection<SelectDataVo>> getBathDictCode(Set<String> categoryTypes) {
        Map<String, Collection<SelectDataVo>> result = Maps.newHashMap();
        categoryTypes.forEach(categoryType -> {
            List<SelectDataVo> localCodeList = getLocalDictCode(categoryType, null);
            result.put(categoryType, localCodeList);
        });
        return result;
    }
}
