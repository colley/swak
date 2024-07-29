package com.swak.excel.metadata;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.swak.common.util.StringPool;
import com.swak.excel.styler.ExcelExportStyler;
import com.swak.excel.styler.ExcelExportStylerDefaultImpl;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.hssf.util.HSSFColor;

import java.util.List;

/**
 * WriteExcelParams.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
@Data
@Accessors(chain = true)
public class WriteExcelParams implements java.io.Serializable {
	private static final long serialVersionUID = -7979245137149248257L;
	public static int USE_SX_SSF_LIMIT = 1000000;

	/**
	 * 表头颜色 & 标题颜色
	 */
	private short color = HSSFColor.HSSFColorPredefined.WHITE.getIndex();
	/**
	 * 第二行标题颜色 属性说明行的颜色 例如:HSSFColor.SKY_BLUE.index 默认
	 */
	private short headerColor = HSSFColor.HSSFColorPredefined.BLUE_GREY.getIndex();

	private int maxNum = 0;

	private ExcelTypeEnum excelType = ExcelTypeEnum.XLSX;

	private String sheetName = "report";

	private boolean wrap = true;

	/**
	 * 头部行高
	 */
	private Float headHeight = Float.valueOf(25);

	/**
	 * 	内容的行高
 	 */
	private  Float contentHeight = Float.valueOf(20);

	/**
	 * 是否自动列宽
	 */
	private boolean autoColumnWidth = true;

	/**
	 * 根据模板导出
	 */
	private String templatePath;

	// 密码
	private String password;

	/**
	 * 指定导出字段和顺序
	 */
	private List<IncludeField> includeFields;

	private ExcelExportStyler excelExportStyler = new ExcelExportStylerDefaultImpl();

	private boolean useI18n = true;

	private String i18nKeyPrefix = StringPool.EMPTY;

	public WriteExcelParams(ExcelTypeEnum excelType) {
		this.excelType = excelType;
	}

	public WriteExcelParams(String templatePath) {
		this.templatePath = templatePath;
	}

	public WriteExcelParams() {

	}
}
