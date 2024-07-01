package com.swak.autoconfigure.runner;

import com.swak.common.enums.EnumType;
import com.swak.common.enums.LocalTypeRepository;
import com.swak.core.aware.InitializedAware;
import com.swak.core.environment.SystemEnvironmentConfigurable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * @author colley
 */
@Slf4j
@Component
public class LocalEnumTypeInitializedAware implements InitializedAware {

    @Autowired(required = false)
    private  SystemEnvironmentConfigurable systemConfig;

    @Override
    public void afterInstantiated() {
        if(Objects.isNull(systemConfig)) {
            log.warn("SystemEnvironmentConfigurable is null please config and should  basePackage and initializeLocalType is not null");
            return;
        }
        if(systemConfig.getInitializeLocalType() &&
                ArrayUtils.isNotEmpty(systemConfig.getBasePackages())) {
            Reflections reflections =
                    new Reflections(new ConfigurationBuilder().forPackages(systemConfig.getBasePackages())
                            .addScanners(Scanners.SubTypes,Scanners.TypesAnnotated));
            Set<Class<?>> localTypes = reflections.get(Scanners.SubTypes.of(EnumType.class).asClass());
            if(CollectionUtils.isEmpty(localTypes)) {
                return;
            }
            for (Class<?> clazz : localTypes) {
                LocalTypeRepository.doRegistration(clazz.getSimpleName(), (Class<? extends EnumType>) clazz);
            }
        }
    }
}
