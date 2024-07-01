package com.swak.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.swak.excel.metadata.WriteExcelParams;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.*;

public class DynamicCustomerHandler implements CellWriteHandler {

    public static String placeholderPrefix = "${";
    PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper(placeholderPrefix, "}", ":", true);
    private Map<String, String> dynamicHeader;

    private WriteExcelParams writeExcelParams;


    public DynamicCustomerHandler(Map<String, String> dynamicHeader, WriteExcelParams writeExcelParams) {
        this.dynamicHeader = dynamicHeader;
        this.writeExcelParams = writeExcelParams;
    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
        if (!isHead || Objects.isNull(head)) {
            return;
        }
        List<String> headNameList = head.getHeadNameList();
        if (CollectionUtils.isNotEmpty(headNameList)) {
            String oldHeadName = headNameList.get(0);
            int startIndex = oldHeadName.indexOf(placeholderPrefix);
            if (startIndex == -1) {
                return;
            }
            Properties properties = new Properties();
            Optional.ofNullable(dynamicHeader).orElse(Collections.emptyMap())
                    .forEach((k, v) -> {
                        if (Objects.nonNull(k) && Objects.nonNull(v)) {
                            properties.put(k, v);
                            i18nKeyHandler(properties,k,v);

                        }
                    });
            headNameList.set(0, placeholderHelper.replacePlaceholders(headNameList.get(0), properties));
        }
    }

    private void i18nKeyHandler(Properties properties, String key, Object value) {
        if (StringUtils.isEmpty(writeExcelParams.getI18nKeyPrefix())) {
            return;
        }
        String i18nKeyPrefix = writeExcelParams.getI18nKeyPrefix();
        if (key.startsWith(i18nKeyPrefix + ".")) {
            String lastKey = key.substring(i18nKeyPrefix.length() + 1);
            properties.put(lastKey, value);
        }
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("name1", "colley");
        String value = "${name:11}";
        String i18nKeyPrefix = "message";
        String key = "message";
        if (key.startsWith(i18nKeyPrefix + ".")) {
            System.out.println(key.substring(i18nKeyPrefix.length() + 1));
        } else {
            System.out.println(key);
        }
        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper(placeholderPrefix, "}", ":", true);
        System.out.println(placeholderHelper.replacePlaceholders("${name:11}", properties));
    }
}
