package com.swak.autoconfigure.service;

import com.swak.common.dto.SelectDataVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public interface LocalDictionaryService {

    List<SelectDataVo> getLocalDictCode(String categoryType, String rel);

    Map<String, Collection<SelectDataVo>> getBathDictCode(Set<String> categoryType);
}
