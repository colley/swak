
package com.swak.excel.styler;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.swak.excel.metadata.WriteExcelParams;
import org.apache.poi.ss.usermodel.BorderStyle;

public class ExcelExportStylerDefaultImpl implements ExcelExportStyler {
	
	@Override
	public WriteCellStyle getHeaderStyle(WriteExcelParams writeParams) {
		WriteCellStyle headWriteCellStyle = new WriteCellStyle();
		// 背景设置为红⾊
		headWriteCellStyle.setFillForegroundColor(writeParams.getHeaderColor());
		WriteFont headWriteFont = new WriteFont();
		headWriteFont.setFontHeightInPoints((short) 12);
		headWriteFont.setColor(writeParams.getColor());
		headWriteCellStyle.setWriteFont(headWriteFont);
		return headWriteCellStyle;
	}

	@Override
	public WriteCellStyle getContentStyles(WriteExcelParams writeParams) {
		// 内容的策略
		WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
		contentWriteCellStyle.setBorderBottom(BorderStyle.THIN); // 底边框
		contentWriteCellStyle.setBorderLeft(BorderStyle.THIN); // 左边框
		contentWriteCellStyle.setBorderRight(BorderStyle.THIN); // 右边框
		contentWriteCellStyle.setBorderTop(BorderStyle.THIN); // 顶边框
		contentWriteCellStyle.setWrapped(writeParams.isWrap());

		WriteFont contentWriteFont = new WriteFont();
		// 字体⼤⼩
		contentWriteFont.setFontHeightInPoints((short) 10);
		contentWriteCellStyle.setWriteFont(contentWriteFont);
		return contentWriteCellStyle;
	}

}
