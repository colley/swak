package com.swak.core.support;

import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class ClassScanners {

    public static <T> Set<Class<T>> scanners(String[] basePackages, Class<T> clazz) {
        Reflections reflections =
                new Reflections(new ConfigurationBuilder().forPackages(basePackages)
                        .addScanners(Scanners.SubTypes, Scanners.TypesAnnotated));
        Set<Class<?>> localTypes = reflections.get(Scanners.SubTypes.of(clazz).asClass());
        Set<Class<T>> result = Sets.newHashSet();
        Optional.ofNullable(localTypes).orElse(Collections.emptySet())
                .forEach(aClass -> result.add((Class<T>) aClass));
        return result;
    }
}
