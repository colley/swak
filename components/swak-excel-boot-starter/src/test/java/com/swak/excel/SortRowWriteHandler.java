package com.swak.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.property.ExcelWriteHeadProperty;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class SortRowWriteHandler implements RowWriteHandler {

    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer rowIndex, Integer relativeRowIndex, Boolean isHead) {
        if (isHead) {
            // 获取传入的包含的列(字段的名字list),将headMap的索引按照传入的列的顺序重新放入,即可实现排序
            ExcelWriteHeadProperty excelWriteHeadProperty = writeSheetHolder.getExcelWriteHeadProperty();
            Map<Integer, Head> headMap = excelWriteHeadProperty.getHeadMap();
            Collection<String> includeColumnFieldNames = writeSheetHolder.getIncludeColumnFieldNames();
            // 将headMap中的字段名字对应Head
            Map<String, Head> fieldNameHead = headMap.values().stream().collect(Collectors.toMap(Head::getFieldName, head -> head));
            int index = 0;
            for (String includeColumnFieldName : includeColumnFieldNames) {
                // 按照includeColumnFieldNames中的顺序取出head重新覆盖
                Head head = fieldNameHead.get(includeColumnFieldName);
                if (head == null) {
                    continue;
                }
                headMap.put(index++, head);
            }
        }
    }
}
