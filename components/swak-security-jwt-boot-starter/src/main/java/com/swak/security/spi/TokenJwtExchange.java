package com.swak.security.spi;

import com.swak.common.spi.SpiPriority;
import com.swak.common.spi.SpiServiceFactory;

/**
 * TokenJwtExchange.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
public interface TokenJwtExchange extends SpiPriority {

    /**
     * storeTokenJwt
     */
    void storeTokenJwt(String token, String tokenJwt, Long expireSeconds);

    /**
     * takeTokenJwt
     *
     */
    String takeTokenJwt(String token);


    /**
     * 刷新
     */
    default void refresh(String token, String userJwt,Long expireSecond) {}

    default void remove(String token) {
    }

    static TokenJwtExchange getTokenJwtExchange() {
        return SpiServiceFactory.loadFirst(TokenJwtExchange.class);
    }
}
