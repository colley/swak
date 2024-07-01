
package com.swak.archiver.spring.configuration;


import com.swak.archiver.executor.ClickArchiverEngineExecutor;
import com.swak.archiver.executor.MysqlArchiverEngineExecutor;
import com.swak.archiver.common.SwakTemplateExecutor;
import com.swak.archiver.common.SwakTemplateExecutorImpl;
import com.swak.archiver.conf.ArchiverProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@ConditionalOnBean(value = ArchiverProperties.class)
public class ArchiverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MysqlArchiverEngineExecutor.class)
    @ConditionalOnBean(ArchiverProperties.class)
    public MysqlArchiverEngineExecutor mysqlArchiverDataEngine(ArchiverProperties archiverProperties) {
        SwakTemplateExecutor mysqlTemplateExecutor = null;
        if(Objects.nonNull(archiverProperties.getMysqlDataSource())) {
             mysqlTemplateExecutor = new SwakTemplateExecutorImpl(archiverProperties.getMysqlDataSource(),true);
        }
        return new MysqlArchiverEngineExecutor(mysqlTemplateExecutor,archiverProperties.getMonitor());
    }

    @Bean
    @ConditionalOnMissingBean(ClickArchiverEngineExecutor.class)
    @ConditionalOnBean(ArchiverProperties.class)
    public ClickArchiverEngineExecutor clickArchiverDataEngine(ArchiverProperties archiverProperties) {
        SwakTemplateExecutor mysqlTemplateExecutor = null;
        if(Objects.nonNull(archiverProperties.getMysqlDataSource())) {
            mysqlTemplateExecutor = new SwakTemplateExecutorImpl(archiverProperties.getClickhouseDataSource(),false);
        }
        return new ClickArchiverEngineExecutor(mysqlTemplateExecutor,archiverProperties.getMonitor());
    }
}