package com.swak.excel.service;

import com.swak.common.dto.Response;
import com.swak.common.validation.ValidationResult;
import com.swak.excel.metadata.BaseRow;
import com.swak.excel.metadata.ExcelWriteData;
import com.swak.excel.metadata.RowErrResult;

import java.util.List;

/**
 * Excel import and export Handler Service
 * @author colley.ma
 * @date 2022/9/20 11:55
 */
public interface ExcelHandlerService<Q, C,S extends BaseRow> {

	/**
	 * 导出
	 * 
	 * @param query
	 * @return
	 */
	default ExcelWriteData<S> getExportData(Q query) {
		throw new UnsupportedOperationException("unsupported excel file export");
	}

	/**
	 * 导入
	 * 
	 * @param commandInput
	 * @param sheetData
	 * @return
	 */
	default Response<RowErrResult> importRegister(C commandInput, List<S> sheetData) {
		throw new UnsupportedOperationException("unsupported excel upload import");
	}
	
	/**
	 * 校验
	 * @param rowDataList
	 * @return
	 */
	default  ValidationResult validate(C commandInput, List<S> rowDataList) {
		return new ValidationResult(true);
	}
}
