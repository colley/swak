package com.swak.common.spi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * JdkServiceLoader
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class JdkServiceLoader implements SwakServiceLoader {
    @Override
    public <S> List<S> load(Class<S> service) {
        Map<String, S> ultimateMap = Maps.newHashMap();
        ServiceLoader.load(service).forEach(bean ->
                ultimateMap.put(bean.getClass().getName(), bean));
        return Lists.newArrayList(ultimateMap.values());
    }
}
