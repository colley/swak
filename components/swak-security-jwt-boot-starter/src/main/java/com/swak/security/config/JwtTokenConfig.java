package com.swak.security.config;


import com.swak.core.SwakConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@Data
@ConfigurationProperties(prefix = SwakConstants.SWAK_SECURITY)
public class JwtTokenConfig {

    private Boolean enabled;

    private Token token = new Token();

    private String loginUrl = "/login";

    private String logoutUrl="/logout";

    private String successUrl;

    private  Boolean addressEnabled;

    private List<String> permitUrls;

    @Data
    public static  class  Token {
        /**
         * 令牌自定义标识
         */
       private String  header ="Authorization";

        private String refreshToken = "refresh_token";
        /**
         * 令牌密钥
         */
        private String secret = "SWAK@!#";
        /**
         * 令牌有效期（默认120分钟） 单位(s)
         */
        private  Long expireTime = 7200L;
    }
}
