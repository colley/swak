package com.swak.core.spi;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SwakServiceLoader;
import com.swak.core.support.SpringBeanFactory;
import org.apache.commons.collections4.MapUtils;
import org.springframework.aop.framework.AopProxyUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SpringServiceLoader
 *
 * @author colley.ma
 * @since 2.4.0
 */
public class SpringServiceLoader implements SwakServiceLoader {
    @Override
    public <S> List<S> load(Class<S> service) {
        //判断如果是否使用spring load
        if (!SpringBeanFactory.hasSpringContext()) {
            //非spring模式
            return Collections.emptyList();
        }
        Map<String, S> ultimateMap = Maps.newHashMap();
        Map<String, S> beansOfType = SpringBeanFactory.getBeansOfType(service);
        if (MapUtils.isNotEmpty(beansOfType)) {
            beansOfType.values().forEach(bean -> {
                Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
                ultimateMap.put(targetClass.getName(), bean);//覆盖掉
            });
        }
        return Lists.newArrayList(ultimateMap.values());
    }

    @Override
    public int priority() {
        return SpiPriority.SPI_PRIORITY + 1;
    }
}
