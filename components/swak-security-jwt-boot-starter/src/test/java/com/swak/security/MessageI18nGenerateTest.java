package com.swak.security;

import com.swak.common.enums.EnumType;
import com.swak.common.enums.IResultCode;
import com.swak.i18n.MessageI18nSourceScanners;

import java.io.IOException;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class MessageI18nGenerateTest {

    public static void main(String[] args) throws IOException {
        String result = MessageI18nSourceScanners.i18nScanner(new String[]{"com.swak.security"}, IResultCode.class);
        System.out.println(result);
        String resultw = MessageI18nSourceScanners.i18nScanner(new String[]{"com.swak.security"}, EnumType.class);
        System.out.println(resultw);


    }
}
