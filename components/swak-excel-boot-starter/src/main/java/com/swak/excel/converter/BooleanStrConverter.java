package com.swak.excel.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.google.common.collect.Lists;
import com.swak.common.enums.Married;
import com.swak.common.util.GetterUtil;
import com.swak.common.util.StringPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooleanStrConverter implements Converter<Boolean> {

	private static BooleanStrConverter instance = new BooleanStrConverter();

	public static final List<String> VALUE;

	public static Map<String, String> toJavaMap = new HashMap<>();
	static {
		toJavaMap.put("Yes","true");
		toJavaMap.put("No", "false");
		VALUE = Lists.newArrayList(toJavaMap.keySet());
	}

	@Override
	public Class<?> supportJavaTypeKey() {
		return Boolean.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Boolean convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return GetterUtil.getBoolean(toJavaMap.getOrDefault(cellData.getStringValue(), cellData.getStringValue()), false);
	}

	@Override
	public WriteCellData<?> convertToExcelData(Boolean value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		if(value==null) {
			new WriteCellData<>(StringPool.EMPTY);
		}
		return new WriteCellData<>(value.booleanValue()?"Yes":"No");
	}
	
	public static BooleanStrConverter getConverter() {
		return instance;
	}
}
