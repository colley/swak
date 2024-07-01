package com.swak.excel;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.swak.excel.metadata.ExcelWriteData;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;

/**
 * DataValidationCellWriteHandler.java
 *
 * @author colley.ma
 * @since  2022/9/20 13:27
 */
public class DataValidationCellWriteHandler implements CellWriteHandler {

    public static final int MAX_ROW = 200;
    private ExcelWriteData<?> excelWriteData;
    /**
     * 样式类
     */
    private CellStyle cellStyle = null;
    private WriteWorkbookHolder writeWorkbookHolder;

    public DataValidationCellWriteHandler(ExcelWriteData<?> excelWriteData) {
        this.excelWriteData = excelWriteData;
    }

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                 Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {
        if (this.cellStyle == null) {
            this.cellStyle = writeSheetHolder.getSheet().getWorkbook().createCellStyle();
        }
        this.writeWorkbookHolder = writeSheetHolder.getParentWriteWorkbookHolder();
    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell,
                                Head head, Integer relativeRowIndex, Boolean isHead) {
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        Workbook workbook = writeWorkbookHolder.getWorkbook();
        String[] dropDownDatas = excelWriteData.getValidSelect(head.getFieldName());
        if (ArrayUtils.isEmpty(dropDownDatas)) {
            return;
        }
        int col = cell.getColumnIndex();
        DataValidation dataValidation = null;
        String sheetname = sheet.getSheetName();
        String dropSheetName = sheetname + "_dropDownsheet" + col;
        if (dropDownDatas.length > 200) {// 采用引用的方式建立下拉
            Sheet dropListSheet = workbook.getSheet(dropSheetName);
            if (dropListSheet == null) {
                dropListSheet = workbook.createSheet(dropSheetName);
                int i = 0;
                for (String item : dropDownDatas) {
                    Row droprow = dropListSheet.createRow(i);
                    Cell dropcell = droprow.createCell(0);
                    dropcell.setCellValue(item);
                    i++;
                }
            }
            String dropDataListname = sheetname + "_dropDownlist" + cell.getColumnIndex();
            Name nameCell = workbook.getName(dropDataListname);
            if (nameCell == null) {
                nameCell = workbook.createName();
                nameCell.setNameName(dropDataListname);
                String strFormula = dropSheetName + "!$A$1:$A$" + dropDownDatas.length;
                nameCell.setRefersToFormula(strFormula);
                DataValidationConstraint constraint = helper.createFormulaListConstraint(strFormula);
                CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, MAX_ROW, col, col);
                dataValidation = helper.createValidation(constraint, cellRangeAddressList);
            }
        } else {
            // 区间设置
            CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, MAX_ROW, col, col);
            // 下拉内容
            DataValidationConstraint constraint = helper.createExplicitListConstraint(dropDownDatas);
            dataValidation = helper.createValidation(constraint, cellRangeAddressList);
        }
        // Note the check on the actual type of the DataValidation object.
        // If it is an instance of the XSSFDataValidation class then the
        // boolean value 'false' must be passed to the setSuppressDropDownArrow()
        // method and an explicit call made to the setShowErrorBox() method.
        if (dataValidation != null) {
            if (dataValidation instanceof XSSFDataValidation) {
                dataValidation.setSuppressDropDownArrow(true);
                dataValidation.setShowErrorBox(true);
            } else {
                dataValidation.setSuppressDropDownArrow(false);
            }
            int hideIndex = workbook.getSheetIndex(dropSheetName);
            if (hideIndex > -1) {
                workbook.setSheetHidden(hideIndex, true);
            }
            sheet.addValidationData(dataValidation);
        }
    }
}
