package com.swak.core.spi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SwakServiceLoader;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * LocalServiceLoader
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class LocalServiceLoader implements SwakServiceLoader {
    private static ArrayListMultimap<Class<?>,Object> SERVICE_CACHE = ArrayListMultimap.create();

    @Override
    public <S> List<S> load(Class<S> service) {
        List<Object> serviceList = SERVICE_CACHE.get(service);
        if(CollectionUtils.isEmpty(serviceList)){
            return Collections.emptyList();
        }
        Map<String, S> ultimateMap = Maps.newHashMap();
        for (Object svi : serviceList) {
            ultimateMap.put(svi.getClass().getName(), (S)svi);//覆盖掉
        }
        return Lists.newArrayList(ultimateMap.values());
    }

    public static <S>  void addServices(Class<S> sviClazz,S... services){
        if(Objects.nonNull(services)){
            for (S service : services) {
                SERVICE_CACHE.put(sviClazz,service);
            }
        }
    }

    @Override
    public int priority() {
        return SpiPriority.SPI_PRIORITY + 2;
    }

    public static void main(String[] args) {
        LocalServiceLoader serviceLoader = new LocalServiceLoader();
        LocalServiceLoader.addServices(SwakServiceLoader.class,serviceLoader,new SpringServiceLoader());

        List<SwakServiceLoader> swakServiceLoaders = serviceLoader.load(SwakServiceLoader.class);
        for (SwakServiceLoader swakServiceLoader : swakServiceLoaders) {
            System.out.println(swakServiceLoader.getClass().getName());
        }
    }
}
