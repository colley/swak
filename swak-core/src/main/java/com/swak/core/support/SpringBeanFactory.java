
package com.swak.core.support;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * SpringBeanFactory
 *
 * @author colley.ma
 * @date 2022/07/13 17:02:06
 */
@Slf4j
public class SpringBeanFactory implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ApplicationContextUtil.setApplicationContext(context);
    }

    public static ApplicationContext getContext() {
        return ApplicationContextUtil.getContext();
    }

    public static boolean hasSpringContext() {
        return ApplicationContextUtil.hasSpringContext();
    }

    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        if (ApplicationContextUtil.getContext() == null) {
            return null;
        }
        Map<String, T> beansmaps = ApplicationContextUtil.getContext().getBeansOfType(clazz);
        return beansmaps;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        if (ApplicationContextUtil.getContext() == null) {
            return null;
        }
        return (T) ApplicationContextUtil.getContext().getBean(beanName);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        Map<String, T> beansOfType = getBeansOfType(clazz);

        if (MapUtils.isNotEmpty(beansOfType)) {
            return beansOfType.get(beanName);
        }

        return null;
    }

    public static <T> T getBean(Class<T> requiredType) {
        try {
            return getService(requiredType);
        } catch (Exception e) {
            log.error("获取bean失败!", e);
        }
        return null;
    }

    public static <T> T getService(Class<T> requiredType) {
        if (ApplicationContextUtil.getContext() == null) {
            return null;
        }
        String[] beanNames = ApplicationContextUtil.getContext().getBeanNamesForType(requiredType);

        if (beanNames.length == 0) {
            throw new NoSuchBeanDefinitionException(requiredType, "");
        }

        if (beanNames.length > 1) {
            throw new NoSuchBeanDefinitionException(requiredType, "expected single bean but found "
                    + beanNames.length + ": " + StringUtils.arrayToCommaDelimitedString(beanNames));
        }

        return getBean(beanNames[0], requiredType);
    }

    private static class ApplicationContextUtil {
        private static ApplicationContext applicationContext;

        public static void setApplicationContext(ApplicationContext context) {
            applicationContext = context;
        }

        public static boolean hasSpringContext() {
            return Objects.nonNull(applicationContext);
        }

        public static ApplicationContext getContext() {
            if (applicationContext == null) {
                throw new IllegalStateException(
                        "'applicationContext' property is null,ApplicationContextHolder not yet init.");
            } else {
                return applicationContext;
            }
        }
    }
}
