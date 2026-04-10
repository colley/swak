package com.swak.i18n;

import com.swak.common.dto.base.I18nCode;
import com.swak.common.util.OrderedProperties;
import com.swak.core.support.ClassScanners;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class MessageI18nSourceScanners {


    public static String i18nScanner(String[] basePackages, Class<? extends I18nCode> clazz, Class<? extends I18nCode>... filters) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Set<? extends Class<? extends I18nCode>> scanners = ClassScanners.scanners(basePackages, clazz);
        for (Class<? extends I18nCode> scanner : scanners) {
            if (!filter(scanner, filters)) {
                continue;
            }
            OrderedProperties i18nMessage = new OrderedProperties();
            I18nCode[] enumConstants = scanner.getEnumConstants();
            Arrays.stream(enumConstants).forEach(enumConstant -> {
                String className = enumConstant.getClass().getName() + "." + enumConstant;
                i18nMessage.put(className, enumConstant.fallback());
            });
            Arrays.stream(enumConstants).forEach(enumConstant -> {
                String className = enumConstant.getClass().getName() + "." + enumConstant;
                if(StringUtils.isNotBlank(enumConstant.getAlias())){
                    i18nMessage.put(className+".Alias", enumConstant.getAlias());
                }
            });
            StringWriter writer = new StringWriter();
            i18nMessage.store(writer, scanner.getName());
            stringBuilder.append(writer);
        }
        return stringBuilder.toString();
    }

    private static boolean filter(Class<? extends I18nCode> scanner, Class<? extends I18nCode>... filters) {
        if (ArrayUtils.isEmpty(filters)) {
            return true;
        }
        for (Class<? extends I18nCode> filter : filters) {
            if (Objects.equals(filter.getName(), scanner.getName())) {
                return true;
            }
        }
        return false;
    }
}
