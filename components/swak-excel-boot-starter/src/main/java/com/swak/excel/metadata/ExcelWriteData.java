package com.swak.excel.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

@Data
@Accessors(chain = true)
public class ExcelWriteData<T> implements java.io.Serializable {
	private static final long serialVersionUID = 125034374585574874L;

	private String sheetName;

	private Class<?> headClazz;

	private Collection<String> includeColumnFieldNames;

	private Map<String, String> dynamicTitleHeader;

	private Collection<T> data = new ArrayList<>();

	private Map<String, List<String>> dropSelect = new HashMap<>();

	
	public void addValidSelect(String headerName, List<String> dropSel) {
		if (CollectionUtils.isNotEmpty(dropSel)) {
			dropSelect.put(headerName, dropSel);
		}
	}

	public String[] getValidSelect(String headerName) {
		List<String> list = dropSelect.get(headerName);
		return CollectionUtils.isEmpty(list) ? null : list.toArray(new String[list.size()]);
	}
	
	public static <T> ExcelWriteData<T> newWriteData(Collection<T> data){
		ExcelWriteData<T> excelWriteData = new ExcelWriteData<T>();
		excelWriteData.setData(data);
		return excelWriteData;
	}

	public static <T> ExcelWriteData<T> newWriteData(Collection<T> data,Class<?> headClazz){
		ExcelWriteData<T> excelWriteData = newWriteData(data);
		excelWriteData.setHeadClazz(headClazz);
		return excelWriteData;
	}

	public static <T> ExcelWriteData<T> newWriteData(Collection<T> data,Class<?> headClazz,
													 Collection<String> includeColumnFieldNames){
		ExcelWriteData<T> excelWriteData = newWriteData(data,headClazz);
		excelWriteData.setIncludeColumnFieldNames(includeColumnFieldNames);
		return excelWriteData;
	}

	public static <T> ExcelWriteData<T> newWriteData(Collection<T> data,Class<?> headClazz,String sheetName){
		ExcelWriteData<T> excelWriteData = newWriteData(data,headClazz);
		excelWriteData.setSheetName(sheetName);
		return excelWriteData;
	}

	public static <T> ExcelWriteData<T> newWriteData(Collection<T> data,Class<?> headClazz,String sheetName,
													 Collection<String> includeColumnFieldNames){
		ExcelWriteData<T> excelWriteData = newWriteData(data,headClazz,sheetName);
		excelWriteData.setIncludeColumnFieldNames(includeColumnFieldNames);
		return excelWriteData;
	}

}
