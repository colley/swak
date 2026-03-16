package com.swak.easyjob.spring.configuration;

import com.swak.core.environment.SystemEnvironmentConfigurable;
import com.swak.easyjob.EasyJobConfig;
import com.swak.easyjob.EasyScheduledConfigurerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(value = EasyJobConfig.class)
public class EasyJobAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EasyScheduledConfigurerFactory.class)
    @ConditionalOnBean(EasyJobConfig.class)
    public EasyScheduledConfigurerFactory easyScheduledConfigurerFactory(EasyJobConfig easyJobConfig, SystemEnvironmentConfigurable systemConfig) {
        if (StringUtils.isEmpty(easyJobConfig.getAppName())) {
            easyJobConfig.setAppName(systemConfig.getAppName());
        }
        //本地环境自动增加环境后缀
        if (systemConfig.isLocal()) {
            easyJobConfig.setAppName(easyJobConfig.getAppName() + "_" + systemConfig.getCurrentEnv());
        }
        return new EasyScheduledConfigurerFactory(easyJobConfig,systemConfig);
    }
}
