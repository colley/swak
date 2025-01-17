package com.swak.jdbc.spring.configuration;

import com.swak.jdbc.spi.DefaultSwakJdbcTemplate;
import com.swak.jdbc.spi.SwakJdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnBean(DataSource.class)
public class SwakJdbcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SwakJdbcTemplate.class)
    public SwakJdbcTemplate swakJdbcTemplate(DataSource dataSource) {
        return new DefaultSwakJdbcTemplate(dataSource);
    }
}
