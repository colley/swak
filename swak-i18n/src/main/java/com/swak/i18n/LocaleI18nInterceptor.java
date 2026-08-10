package com.swak.i18n;

import com.swak.common.dto.RequestContext;
import com.swak.core.web.SwakInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE;

/**
 * LocaleI18n.java
 *
 * @author colley.ma
 * @since 2.3.3
 */
@Slf4j
public class LocaleI18nInterceptor implements SwakInterceptor {


	@Getter
	private String[] httpMethods;

	@Getter
	private boolean ignoreInvalidLocale = true;

	@Setter
	@Getter
	private String paramName = "Lang";

	@Setter
	private LocaleResolver localeResolver;

	public LocaleI18nInterceptor(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}


	public void setHttpMethods(@Nullable String... httpMethods) {
		this.httpMethods = httpMethods;
	}

	public void setIgnoreInvalidLocale(boolean ignoreInvalidLocale) {
		this.ignoreInvalidLocale = ignoreInvalidLocale;
	}

	private boolean checkHttpMethod(String currentMethod) {
		String[] configuredMethods = getHttpMethods();
		if (ObjectUtils.isEmpty(configuredMethods)) {
			return true;
		}
		for (String configuredMethod : configuredMethods) {
			if (configuredMethod.equalsIgnoreCase(currentMethod)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response) {
		String newLocale = I18nLocaleContext.getLocaleLanguage(request, getParamName());
		if (StringUtils.isNotEmpty(newLocale)) {
			request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
			if (checkHttpMethod(request.getMethod())) {
				LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
				if (localeResolver == null) {
					throw new IllegalStateException(
							"[Swak-I18n] No LocaleResolver found: not in a DispatcherServlet request?");
				}
				try {
					localeResolver.setLocale(request, response, I18nLocaleContext.getLocale(newLocale));
				} catch (IllegalArgumentException ex) {
					if (isIgnoreInvalidLocale()) {
						if (log.isDebugEnabled()) {
							log.debug("[Swak-I18n] Ignoring invalid locale value [{}]: {}", newLocale, ex.getMessage());
						}
					} else {
						throw ex;
					}
				}
			}
			RequestContext.getContext().setRequestLanguage(newLocale);
		}
		return true;
	}

	@Override
	public int priority() {
		return 3;
	}
}
