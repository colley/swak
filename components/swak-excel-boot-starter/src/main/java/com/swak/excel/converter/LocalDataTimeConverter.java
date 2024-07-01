package com.swak.excel.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.alibaba.excel.util.DateUtils;

import java.util.Date;

public class LocalDataTimeConverter implements Converter<Date> {

	private static final String PARSE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	@Override
	public Class<?> supportJavaTypeKey() {
		return Date.class;
	}

	@Override
	public CellDataTypeEnum supportExcelTypeKey() {
		return CellDataTypeEnum.STRING;
	}

	@Override
	public Date convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return DateUtils.parseDate(cellData.getStringValue());
	}

	@Override
	public WriteCellData<?> convertToExcelData(Date value, ExcelContentProperty contentProperty,
			GlobalConfiguration globalConfiguration) throws Exception {
		return new WriteCellData<>(DateUtils.format(value, PARSE_PATTERN));
	}
}
