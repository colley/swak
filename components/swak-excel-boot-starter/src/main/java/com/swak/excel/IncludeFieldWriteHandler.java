package com.swak.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.property.ExcelWriteHeadProperty;
import com.google.common.collect.Maps;
import com.swak.excel.metadata.IncludeField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IncludeFieldWriteHandler implements RowWriteHandler {

    private List<IncludeField> includeFields;

    public IncludeFieldWriteHandler(List<IncludeField> includeFields) {
        this.includeFields = includeFields;
    }

    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer rowIndex, Integer relativeRowIndex, Boolean isHead) {
        if (isHead) {
            if (CollectionUtils.isEmpty(includeFields)) {
                return;
            }
            List<IncludeField> sortedIncludeFields = includeFields.stream().sorted(Comparator.comparingInt(IncludeField::getIndex)).collect(Collectors.toList());
            // 获取传入的包含的列(字段的名字list),将headMap的索引按照传入的列的顺序重新放入,即可实现排序
            ExcelWriteHeadProperty excelWriteHeadProperty = writeSheetHolder.getExcelWriteHeadProperty();
            Map<Integer, Head> headMap = excelWriteHeadProperty.getHeadMap();
            // 将headMap中的字段名字对应Head
            Map<String, Head> fieldNameHead = headMap.values().stream().collect(Collectors.toMap(Head::getFieldName, head -> head));
            Map<Integer, Head> newHeadMap = Maps.newHashMap();
            int index = 0;
            for (IncludeField includeField : sortedIncludeFields) {
                // 按照includeColumnFieldNames中的顺序取出head重新覆盖
                Head head = fieldNameHead.get(includeField.getFieldName());
                if (head == null) {
                    continue;
                }
                newHeadMap.put(index, head);
                index++;
            }
            excelWriteHeadProperty.setHeadMap(newHeadMap);
        }
    }
}
