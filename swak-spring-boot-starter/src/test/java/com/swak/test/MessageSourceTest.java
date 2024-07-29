package com.swak.test;

import com.swak.common.enums.IResultCode;
import com.swak.core.support.ClassScanners;

import java.util.Arrays;
import java.util.Set;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class MessageSourceTest {


    public static void main(String[] args) {
        Set<Class<IResultCode>> scanners = ClassScanners.scanners(new String[]{"com.swak"}, IResultCode.class);
        StringBuilder builder = new StringBuilder();
        for (Class<IResultCode> scanner : scanners) {
            IResultCode[] enumConstants = scanner.getEnumConstants();
            Arrays.stream(enumConstants).forEach(enumConstant -> {
                Enum _enumConstant = (Enum) enumConstant;
                String className = enumConstant.getClass().getSimpleName();
                builder.append(className).append(" | ").append(_enumConstant.name()).append(" | ").append(enumConstant.getCode())
                                .append(" | ").append(enumConstant.getMsg()).append(" |").append("\n");
            });
        }
        System.out.println(builder.toString());
    }
}
