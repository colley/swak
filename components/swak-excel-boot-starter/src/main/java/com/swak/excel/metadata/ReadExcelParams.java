package com.swak.excel.metadata;

import lombok.Data;

/**
 * ReadExcelParams.java
 * 
 * @author colley.ma
 * @since 2.4.0
 **/
@Data
public class ReadExcelParams implements java.io.Serializable {
	private static final long serialVersionUID = 5939498044910752625L;
	private Class<?>[] groups = new Class<?>[0];

	public static ReadExcelParams newReadParams(Class<?>... groups) {
		ReadExcelParams readerParam = new ReadExcelParams();
		readerParam.setGroups(groups);
		return readerParam;
	}
}
