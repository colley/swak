package com.swak.excel.builder;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.google.common.collect.Lists;
import com.swak.excel.converter.BooleanStrConverter;
import com.swak.excel.enums.ExcelErrCode;
import com.swak.excel.metadata.ExcelSheetData;
import com.swak.excel.metadata.ReadExcelParams;
import com.swak.excel.metadata.SheetRowData;
import com.swak.excel.validation.BeanDataValidator;
import com.swak.excel.validation.BizRowDataValidator;
import com.swak.excel.validation.ExcelAnalysisDataHandler;
import com.swak.excel.validation.RowDataValidator;
import com.swak.common.exception.ExcelException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class SwakExcelReadBuilder extends ExcelReaderBuilder {

	private final ExcelAnalysisDataHandler excelAnalysisDataHandler;

	private Class<?>[] allSheetHeader = new Class[0];

	public SwakExcelReadBuilder() {
		super();
		this.excelAnalysisDataHandler = new ExcelAnalysisDataHandler();
		super.autoTrim(true);
	}

	private static ExcelTypeEnum getExcelType(String fileName) {
		if ((fileName.endsWith(".csv"))) {
			return ExcelTypeEnum.CSV;
		}
		if(fileName.endsWith(".xls")) {
			return ExcelTypeEnum.XLS;
		}

		if(fileName.endsWith(".xlsx")) {
			return ExcelTypeEnum.XLSX;
		}
		throw new ExcelException(ExcelErrCode.EXCEL_INVALID_TYPE);
	}

	public static SwakExcelReadBuilder read(MultipartFile file) {
		ExcelTypeEnum excelType = getExcelType(file.getOriginalFilename());
		try {
			return read(file.getInputStream(),excelType,null);
		} catch (IOException e) {
			throw new ExcelException(ExcelErrCode.EXCEL_READ_ERROR,e);
		}
	}

	public static SwakExcelReadBuilder read(MultipartFile file,Class<?> head) {
		ExcelTypeEnum excelType = getExcelType(file.getOriginalFilename());
		try {
			return read(file.getInputStream(),excelType,head);
		} catch (IOException e) {
			throw new ExcelException(ExcelErrCode.EXCEL_READ_ERROR,e);
		}
	}

	public static SwakExcelReadBuilder read(InputStream inputStream) {
		return read(inputStream, null);
	}

	public static SwakExcelReadBuilder read(InputStream inputStream,ExcelTypeEnum excelType, Class<?> head) {
		SwakExcelReadBuilder excelReaderBuilder = new SwakExcelReadBuilder();
		excelReaderBuilder.file(inputStream);
		if (head != null) {
			excelReaderBuilder.head(head);
		}
		if(Objects.nonNull(excelType)) {
			excelReaderBuilder.excelType(excelType);
		}
		return excelReaderBuilder;
	}

	public static SwakExcelReadBuilder read(InputStream inputStream, Class<?> head) {
		return  read(inputStream,null,head);
	}

	@Override
    public SwakExcelReadBuilder registerConverter(Converter<?> converter) {
		super.registerConverter(converter);
		return this;
	}

	public SwakExcelReadBuilder registerConverter(List<Converter<?>> converters) {
		if (CollectionUtils.isNotEmpty(converters)) {
			for (Converter<?> converter : converters) {
				super.registerConverter(converter);
			}
		}
		return this;
	}

	public SwakExcelReadBuilder addBizValidator(BizRowDataValidator<?> bizValidator) {
		excelAnalysisDataHandler.addBizValidator(bizValidator);
		return this;
	}

	
	public SwakExcelReadBuilder addDataValidator(RowDataValidator<?> validator) {
		excelAnalysisDataHandler.addDataValidator(validator);
		return this;
	}
	
	public SwakExcelReadBuilder addFirstValidator(RowDataValidator<?> validator) {
		excelAnalysisDataHandler.addFirstDataValidator(validator);
		return this;
	}
	
	public SwakExcelReadBuilder defaultReadHandler() {
		super.registerReadListener(excelAnalysisDataHandler);
		defaultRegisterConverter();
		return this;
	}

	public SwakExcelReadBuilder defaultRegisterConverter() {
		registerConverter(BooleanStrConverter.getConverter());
		return this;
	}
	
	public <T, R> R doRead(Function<List<T>, R> handler) {
		List<T> metadata = doRead();
		return handler.apply(metadata);
	}
	
	public <T> List<T> doRead(){
		try {
			super.sheet().doRead();
			return excelAnalysisDataHandler.readAllData();
		}catch (ExcelException e){
			throw e;
		}catch (Exception e) {
			throw new ExcelException(ExcelErrCode.EXCEL_READ_ERROR,e);
		}
	}

	public SwakExcelReadBuilder multiHead(Class<?> ... clazz) {
		this.allSheetHeader = clazz;
		return this;
	}

	/**
	 * 查询所有的sheet数据
	 */
	public ExcelSheetData doReadAllSheet(ReadExcelParams readExcelParams) {
		ExcelReader excelReader = this.build();
		List<ExcelAnalysisDataHandler> excelAnalysisDataHandlerList = Lists.newArrayListWithCapacity(allSheetHeader.length);
		List<ReadSheet> readSheets = Lists.newArrayListWithCapacity(allSheetHeader.length);
		for(int sheetNo=0;sheetNo<allSheetHeader.length;sheetNo++) {
			ExcelAnalysisDataHandler excelAnalysisDataHandler = new ExcelAnalysisDataHandler(BeanDataValidator.newDataValidator(readExcelParams.getGroups()));
			excelAnalysisDataHandlerList.add(excelAnalysisDataHandler);
			ReadSheet readSheet = EasyExcel.readSheet(sheetNo).head(allSheetHeader[sheetNo]).registerReadListener(excelAnalysisDataHandler).build();
			readSheets.add(readSheet);
		}
		excelReader.read(readSheets.toArray(new ReadSheet[0]));
		ExcelSheetData excelSheetData = new ExcelSheetData();
		excelReader.finish();
		for(int i=0;i<excelAnalysisDataHandlerList.size();i++) {
			ExcelAnalysisDataHandler excelAnalysisDataHandler = excelAnalysisDataHandlerList.get(i);
			SheetRowData sheetRowData = new SheetRowData();
			sheetRowData.setHead(allSheetHeader[i]);
			sheetRowData.setSheetName(readSheets.get(i).getSheetName());
			sheetRowData.setExcelRows(excelAnalysisDataHandler.readAllData());
			excelSheetData.addSheetRows(sheetRowData);
		}
		return excelSheetData;
	}
}
