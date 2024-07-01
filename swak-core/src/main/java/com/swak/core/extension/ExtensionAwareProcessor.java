package com.swak.core.extension;

import com.swak.core.aware.SwakAwareProcessor;
import com.swak.core.extension.repository.ExtensionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Objects;

@Configuration(proxyBeanMethods = false)
@Order(10)
@Slf4j
public class ExtensionAwareProcessor implements SwakAwareProcessor {

	@Autowired(required = false)
	public ExtensionRepository extensionRepository;

	@Override
	public void processor() {
		if(Objects.isNull(extensionRepository)){
			return;
		}
		extensionRepository.getAllExtension().forEach((k, v) -> {
			log.warn("[swak-ExtensionPoint] - ExtensionPoint Initialize info {}={},ExtPt={}",
					k.getBizScenarioUniqueIdentity(), v.getClass().getName(), k.getExtensionPointName());
		});
	}

}
