/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: SwakCommandLineRunner.java
 * Encoding: UTF-8
 * Date: 2021年4月7日 下午2:25:15
 * History:
*/
package com.swak.autoconfigure.runner;

import com.swak.core.aware.DelegatingAwareProcessorComposite;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Order(2)
public class AppCommandLineRunner implements CommandLineRunner {
	
	@Resource
	private DelegatingAwareProcessorComposite delegatingAwareProcessorComposite;

	@Override
	public void run(String... args) throws Exception {
		long startTime = System.currentTimeMillis();
		System.out.println("Swak framework Runner start to initialize ....");
		delegatingAwareProcessorComposite.afterInstantiated();
		System.out.println("Swak framework Runner start to initialized,cost:"+(System.currentTimeMillis()-startTime)+"ms....");
	}
}