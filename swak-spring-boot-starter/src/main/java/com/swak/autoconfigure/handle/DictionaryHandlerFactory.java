package com.swak.autoconfigure.handle;

import com.google.common.collect.Maps;
import com.swak.common.dto.SelectDataVo;
import com.swak.common.spi.SpiServiceFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * DictionaryHandlerFactory.java
 *
 * @author colley.ma
 * @since 2.4.0
 */

@Component
public class DictionaryHandlerFactory implements SmartInitializingSingleton {

    private final Map<String, DictionaryHandler> enumTypeDictHandlerMap = Maps.newHashMap();
    private DictionaryHandler defaultDictionaryHandler;

    public DictionaryHandler getDictionaryHandler(String dictCategory) {
        return enumTypeDictHandlerMap.getOrDefault(dictCategory, defaultDictionaryHandler);
    }

    public List<SelectDataVo> commence(List<SelectDataVo> sources, String dictCategory) {
        if (CollectionUtils.isEmpty(sources)) {
            return sources;
        }
        DictionaryHandler dictionaryHandler = getDictionaryHandler(dictCategory);
        if (Objects.isNull(dictionaryHandler)) {
            return sources;
        }
        List<SelectDataVo> onFilter = dictionaryHandler.onFilter(sources);
        return dictionaryHandler.onSorted(onFilter);
    }

    @Override
    public void afterSingletonsInstantiated() {
        SpiServiceFactory.load(DictionaryHandler.class).forEach(handler -> {
            if (CollectionUtils.isEmpty(handler.dictCategory()) && Objects.isNull(defaultDictionaryHandler)) {
                this.defaultDictionaryHandler = handler;
            }
            handler.dictCategory().forEach(dictCategory ->
                    this.enumTypeDictHandlerMap.put(dictCategory, handler));
        });
    }
}
