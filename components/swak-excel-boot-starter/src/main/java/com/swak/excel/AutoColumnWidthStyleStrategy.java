package com.swak.excel;


import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.alibaba.excel.write.style.row.AbstractRowHeightStyleStrategy;
import com.swak.excel.metadata.WriteExcelParams;
import com.swak.excel.styler.ExcelExportStyler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AutoColumnWidthStyleStrategy.java
 *
 * @author colley.ma
 * @since  2022/9/20 13:29
 */
public class AutoColumnWidthStyleStrategy extends AbstractColumnWidthStyleStrategy {

    private Map<Integer, Map<Integer, Integer>> CACHE = new HashMap<>();

    public static HorizontalCellStyleStrategy createStyleStrategy(WriteExcelParams writeParams) {
        ExcelExportStyler excelExportStyler = writeParams.getExcelExportStyler();
        // 这个策略是头是头的样式内容是内容的样式其他的策略可以⾃⼰实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                new HorizontalCellStyleStrategy(excelExportStyler.getHeaderStyle(writeParams),
                        excelExportStyler.getContentStyles(writeParams));
        return horizontalCellStyleStrategy;
    }

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList, Cell cell,
                                  Head head, Integer relativeRowIndex, Boolean isHead) {
        boolean needSetWidth = isHead || !CollectionUtils.isEmpty(cellDataList);
        if (needSetWidth) {
            Map<Integer, Integer> maxColumnWidthMap = CACHE.get(writeSheetHolder.getSheetNo());
            if (maxColumnWidthMap == null) {
                maxColumnWidthMap = new HashMap<Integer, Integer>();
                CACHE.put(writeSheetHolder.getSheetNo(), maxColumnWidthMap);
            }
            Integer columnWidth = this.dataLength(cellDataList, cell, isHead);
            if (columnWidth >= 0) {
                if (columnWidth > 255) {
                    columnWidth = 255;
                }
                Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
                if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
                    maxColumnWidthMap.put(cell.getColumnIndex(), columnWidth);
                    writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), columnWidth * 256);
                }
            }
        }
    }

    private Integer dataLength(List<WriteCellData<?>> cellDataList, Cell cell, Boolean isHead) {
        if (isHead) {
            // 此处加5可以额外扩充列宽度
            return cell.getStringCellValue().getBytes().length + 5;
        } else {
            WriteCellData<?> cellData = cellDataList.get(0);
            CellDataTypeEnum type = cellData.getType();
            if (type == null) {
                return -1;
            } else {
                switch (type) {
                    case STRING:
                        return cellData.getStringValue().getBytes().length;
                    case BOOLEAN:
                        return cellData.getBooleanValue().toString().getBytes().length;
                    case NUMBER:
                        return cellData.getNumberValue().toString().getBytes().length;
                    default:
                        return -1;
                }
            }
        }
    }

    /**
     * 设置行高
     */
    public static class CustomRowHeightStyleStrategy extends AbstractRowHeightStyleStrategy {
        private WriteExcelParams writeParams;

        public CustomRowHeightStyleStrategy(WriteExcelParams writeParams) {
            this.writeParams = writeParams;
        }

        @Override
        protected void setHeadColumnHeight(Row row, int index) {
            if (Objects.nonNull(writeParams.getHeadHeight())) {
                row.setHeightInPoints(writeParams.getHeadHeight().floatValue());
            }
        }

        @Override
        protected void setContentColumnHeight(Row row, int i) {
            if (Objects.nonNull(writeParams.getContentHeight())) {
                row.setHeightInPoints(writeParams.getContentHeight().floatValue());
            }
        }
    }
}
