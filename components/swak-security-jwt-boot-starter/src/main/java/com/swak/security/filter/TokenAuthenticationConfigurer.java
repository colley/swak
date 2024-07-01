package com.swak.security.filter;

import com.google.common.collect.Lists;
import com.swak.core.security.SwakAuthenticationFilter;
import com.swak.security.authentication.UserTokenService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class TokenAuthenticationConfigurer extends AbstractHttpConfigurer<TokenAuthenticationConfigurer, HttpSecurity> {
    @Override
    public void configure(HttpSecurity builder) {
        UserTokenService userTokenService = builder.getSharedObject(ApplicationContext.class).getBean(UserTokenService.class);
        Map<String, SwakAuthenticationFilter> swakAuthenticationFilters = builder.getSharedObject(ApplicationContext.class).getBeansOfType(SwakAuthenticationFilter.class);
        builder.addFilterBefore(authenticationTokenFilter(userTokenService,swakAuthenticationFilters), UsernamePasswordAuthenticationFilter.class);
    }

    public JwtAuthenticationTokenFilter authenticationTokenFilter(UserTokenService userTokenService,Map<String, SwakAuthenticationFilter> swakAuthenticationFilters) {
        JwtAuthenticationTokenFilter tokenFilter = new JwtAuthenticationTokenFilter();
        tokenFilter.setUserTokenService(userTokenService);
        if(MapUtils.isNotEmpty(swakAuthenticationFilters)) {
            tokenFilter.setAuthenticationFilters(Lists.newArrayList(swakAuthenticationFilters.values()));
        }
        return tokenFilter;
    }
}
