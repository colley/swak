
package com.swak.autoconfigure.config;

import com.swak.core.SwakConstants;
import com.swak.core.web.SwakMvcConfigurer;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.XmlEscape;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;

import javax.servlet.ServletContext;
import java.util.List;

/**
 * SwakWebMvcConfigurer.java
 */
@Order(SwakConstants.ORDER_PRECEDENCE +1)
@Component
public  class SwakWebMvcConfigurer implements SwakMvcConfigurer {

	@Autowired(required = false)
	private freemarker.template.Configuration configuration;

	@Autowired(required = false)
	private ServletContext servletContext;

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		if (configuration != null && servletContext!=null) {
			// 自定义配置信息
			try {
				this.configuration.setSharedVariable("ctx", this.servletContext.getContextPath());
				this.configuration.setSharedVariable("xml_escape", new XmlEscape());
				//this.configuration.setSharedVariable("shiro", new ShiroTags());
			} catch (TemplateModelException e) {
				// ingro
			}
		}
	}

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		for (HttpMessageConverter<?> messageConverter : converters) {
			System.out.println(messageConverter);
		}
	}
}
