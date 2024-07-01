package com.swak.test;

import com.swak.common.util.StringPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Locale;

/**
 * @author colley.ma
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
public class MessageSourceTest {

    @Test
    public void testMessageResource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("i18n/DictMessages","i18n/ResultMessages");
        messageSource.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        messageSource.setUseCodeAsDefaultMessage(false);
        messageSource.setDefaultEncoding(StringPool.UTF8);
        messageSource.setAlwaysUseMessageFormat(false);
        System.out.println(messageSource.getMessage("com.swak.common.enums.BasicErrCode.SUCCESS",null,Locale.ENGLISH));
    }
}
