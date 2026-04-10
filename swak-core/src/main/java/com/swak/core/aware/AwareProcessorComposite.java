package com.swak.core.aware;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AwareProcessorComposite.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
@Slf4j
public class AwareProcessorComposite implements SwakAwareProcessor {


    private final List<SwakAwareProcessor> delegates = Lists.newArrayList();

    public void addAwareProcessors(List<SwakAwareProcessor> configurers) {
        if (!CollectionUtils.isEmpty(configurers)) {
            this.delegates.addAll(configurers);
        }
    }

    @Override
    public void processor() {
        if (CollectionUtils.isEmpty(delegates)) {
            log.error(
                "[swak-awareProcessor] - SwakAwareProcessor init error,configurers is Empty!");
            return;
        }
        for (SwakAwareProcessor delegate : this.delegates) {
        	log.warn("[swak-awareProcessor] - {}.processor", delegate.getClass().getName());
            delegate.processor();
        }
    }
}
