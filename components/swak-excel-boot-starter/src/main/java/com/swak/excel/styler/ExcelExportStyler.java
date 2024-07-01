
package com.swak.excel.styler;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.swak.excel.metadata.WriteExcelParams;


/**
 * Excel导出样式接口
 *
 */
public interface ExcelExportStyler {

	/**
	 * 列表头样式
	 */
	public WriteCellStyle getHeaderStyle(WriteExcelParams writeParams);

	/**
	 * 获取样式方法
	 */
	public WriteCellStyle getContentStyles(WriteExcelParams writeParams);


}
