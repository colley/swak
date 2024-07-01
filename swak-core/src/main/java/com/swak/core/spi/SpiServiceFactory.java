package com.swak.core.spi;

import com.google.common.collect.Maps;
import com.swak.core.support.SpringBeanFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.aop.framework.AopProxyUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class SpiServiceFactory<S extends SpiPriority> {
    private static Map<Class<?>, SpiServiceFactory> SPI_SERVICE_FACTORY = new ConcurrentHashMap<>();

    private Map<String, List<S>> providers = new ConcurrentHashMap<>();

    public static <S> List<S> load(Class<S> service) {
        SpiServiceFactory spiServiceFactory = SPI_SERVICE_FACTORY.
                computeIfAbsent(service, v -> new SpiServiceFactory<>());
        //判断如果是否使用spring load
        if(!SpringBeanFactory.hasSpringContext()){
            //非spring模式
            return spiServiceFactory.loadProviders(service);
        }
        //spring模式,需要加载spring和spi的去重保留，spring的优先级高
        return spiServiceFactory.loadSpringProviders(service);
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
        Map<String,S> ultimateMap = Maps.newHashMap();
        ServiceLoader.load(service).forEach(bean-> ultimateMap.put(bean.getClass().getName(),bean));
        providersList = ultimateMap.values().stream()
                .sorted(Comparator.comparingInt(SpiPriority::priority)).collect(Collectors.toList());
        providers.put(service.getName(), providersList);
        return providersList;
    }

    public List<S> loadSpringProviders(Class<S> service) {
        List<S> providersList = providers.get(service.getName());
        if (Objects.nonNull(providersList)) {
            return providersList;
        }
        Map<String,S> ultimateMap = loadProviders(service).stream()
                .collect(Collectors.toMap(k->k.getClass().getName(),v->v));
        Map<String, S> beansOfType = SpringBeanFactory.getBeansOfType(service);
        if(MapUtils.isNotEmpty(beansOfType)){
            beansOfType.values().forEach(bean->{
                Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
                ultimateMap.put(targetClass.getName(),bean);//覆盖掉
            });
        }
        providersList = ultimateMap.values().stream()
                .sorted(Comparator.comparingInt(SpiPriority::priority)).collect(Collectors.toList());
        providers.put(service.getName(), providersList);
        return providersList;
    }
}
