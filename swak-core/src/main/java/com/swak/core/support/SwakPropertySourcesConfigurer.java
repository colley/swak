
package com.swak.core.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 配置文件封装 ClassName: SwakPropertySourcesConfigurer.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
@Slf4j
public class SwakPropertySourcesConfigurer extends PropertySourcesPlaceholderConfigurer {
    /**
     * Return a merged Properties instance containing both the loaded properties and
     * properties set on this FactoryBean.
     */
    @Override
    protected void loadProperties(Properties result) throws IOException {
        super.loadProperties(result);
        // 设置PropertieConfig
        Properties localProperties = this.convertStringProps(result);
        localProperties(localProperties);
    }

    private Properties convertStringProps(Properties props) {
        Enumeration<?> propertyNames = props.propertyNames();
        Properties localProperties = new Properties();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = props.getProperty(propertyName);
            localProperties.setProperty(propertyName, propertyValue);
        }
        return localProperties;
    }

    protected void localProperties(Properties target) {
        PropertiesFactory.localProperties = target;
        try {
            target.forEach((k, v) -> {
                log.warn("属性配置信息：{}={}", k, v);
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
