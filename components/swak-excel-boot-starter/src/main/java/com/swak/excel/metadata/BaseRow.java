package com.swak.excel.metadata;

import com.alibaba.excel.annotation.ExcelIgnore;

import java.util.List;
import java.util.Map;

public abstract class BaseRow implements ExcelRow {

	@ExcelIgnore
	private Integer rowIndex;

	@ExcelIgnore
	private Map<Integer, List<String>> rowHead;

	@ExcelIgnore
	private String sheetName;

	@Override
	public Integer getRowIndex() {
		return rowIndex;
	}

	@Override
	public String getSheetName() {
		return sheetName;
	}
	@Override
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	@Override
	public void setRowIndex(Integer rowIndex) {
		this.rowIndex = rowIndex;
	}

	@Override
	public Map<Integer, List<String>> getRowHead() {
		return this.rowHead;
	}

	@Override
	public void setRowHead(Map<Integer, List<String>> rowHead) {
		this.rowHead = rowHead;
	}

}