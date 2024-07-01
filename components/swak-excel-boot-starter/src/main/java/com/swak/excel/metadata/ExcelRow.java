package com.swak.excel.metadata;

import java.util.List;
import java.util.Map;

public interface ExcelRow {

	Integer getRowIndex();

	String getSheetName();

	void setSheetName(String sheetName);

	void setRowIndex(Integer rowIndex);

	Map<Integer, List<String>> getRowHead();

	void setRowHead(Map<Integer, List<String>> rowHead);
}
