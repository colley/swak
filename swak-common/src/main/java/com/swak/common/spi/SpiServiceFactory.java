package com.swak.common.spi;

import com.google.common.collect.Maps;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SpiServiceFactory.java
 *
 * @author colley.ma
 * @since 3.0.0
 */
public class SpiServiceFactory<S extends SpiPriority> {
    private static final Map<Class<?>, SpiServiceFactory> SPI_SERVICE_FACTORY = new ConcurrentHashMap<>();
    private final List<SwakServiceLoader> swakServiceLoaders = new ArrayList<>();
    private final Map<String, List<S>> providers = new ConcurrentHashMap<>();

    public static <S> List<S> load(Class<S> service) {
        SpiServiceFactory spiServiceFactory = SPI_SERVICE_FACTORY.
                computeIfAbsent(service, v -> new SpiServiceFactory<>());
        return spiServiceFactory.loadProviders(service);
    }

    public static <S> S loadFirst(Class<S> service) {
        List<S> providersList = load(service);
        if (CollectionUtils.isNotEmpty(providersList)) {
            return providersList.get(0);
        }
        return null;
    }

    public List<S> loadProviders(Class<S> service) {
        List<S> providersList = providers.get(service.getName());
        if (Objects.nonNull(providersList)) {
            return providersList;
        }
        //去重
        Map<String, S> ultimateMap = Maps.newHashMap();
        List<SwakServiceLoader> swakServiceLoaders = loadServiceLoader();
        if (CollectionUtils.isNotEmpty(swakServiceLoaders)) {
            for (SwakServiceLoader swakServiceLoader : swakServiceLoaders) {
                swakServiceLoader.load(service).forEach(bean -> ultimateMap.put(bean.getClass().getName(), bean));
            }
        }
        providersList = ultimateMap.values().stream()
                .sorted(Comparator.comparingInt(SpiPriority::priority)).collect(Collectors.toList());
        providers.put(service.getName(), providersList);
        return providersList;
    }

    private List<SwakServiceLoader> loadServiceLoader() {
        if (CollectionUtils.isNotEmpty(swakServiceLoaders)) {
            return swakServiceLoaders;
        }
        Map<String, SwakServiceLoader> ultimateMap = Maps.newHashMap();
        ServiceLoader.load(SwakServiceLoader.class).forEach(bean -> ultimateMap.put(bean.getClass().getName(), bean));
        List<SwakServiceLoader> serviceLoaders = ultimateMap.values().stream()
                .sorted(Comparator.comparingInt(SwakServiceLoader::priority)).collect(Collectors.toList());
        this.swakServiceLoaders.addAll(serviceLoaders);
        return this.swakServiceLoaders;
    }
}
