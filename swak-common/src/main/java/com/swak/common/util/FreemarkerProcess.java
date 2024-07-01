
package com.swak.common.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;


/**
 * freemarker模板处理
 * @author colley.ma
 * @date 2022/07/13 16:23:50
 *
 */
@Slf4j
public class FreemarkerProcess {
    private Configuration tempConfiguration;
    private FreemarkerProcess(){
        initStringTemplateConfig();
    }

    private void initStringTemplateConfig() {
        tempConfiguration = new Configuration(Configuration.VERSION_2_3_22);
        tempConfiguration.setTemplateLoader(new StringTemplateLoader());
        tempConfiguration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        tempConfiguration.setDateFormat("yyyy-MM-dd");
        tempConfiguration.setTimeFormat("HH:mm:ss");
        tempConfiguration.setNumberFormat("0.####");
        tempConfiguration.setDefaultEncoding(StringPool.UTF8);
        tempConfiguration.setClassicCompatible(true);
    }


    public Template getStringTemplate(String name, String templateContent) throws Exception {
        StringTemplateLoader stringLoader = (StringTemplateLoader) tempConfiguration.getTemplateLoader();
        stringLoader.putTemplate(name, templateContent);
        return tempConfiguration.getTemplate(name, StringPool.UTF8);
    }

    public String stringTemplate2String(Object root, String stringTemplate) {
        String templateName = DigestUtils.md5(stringTemplate);
        return stringTemplate2String(root,templateName,stringTemplate);
    }

    public String stringTemplate2String(Object root, String name, String stringTemplate) {
        log.debug("start generate String");
        StringWriter sw = new StringWriter();
        try {
            Template templateStr = this.getStringTemplate(name, stringTemplate);
            templateStr.process(root, sw);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public static FreemarkerProcess getInstance() {
        return FreemarkerProcessInstance.INSTANCE;
    }

    private static class FreemarkerProcessInstance {
        private static  FreemarkerProcess INSTANCE = new FreemarkerProcess();
    }

}
