package com.swak.common.spi;

import java.util.List;

/**
 * SwakServiceLoader.java
 *
 * @author colley.ma
 * @since 2.4.0
 */
public interface SwakServiceLoader extends SpiPriority {

    <S> List<S> load(Class<S> service);
}
