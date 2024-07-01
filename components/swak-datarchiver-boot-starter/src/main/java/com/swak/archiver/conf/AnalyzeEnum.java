/**
 * Copyright (C), 2020-2021 by colley.ma
 * File Name: AnalyzeEnum.java
 * Encoding: UTF-8
 * Date: 2021年12月24日 下午2:30:29
 * History:
*/
package com.swak.archiver.conf;

public enum AnalyzeEnum {
	ANALYZE("ANALYZE", ANALYZE_TBL.ALL), 
	ANALYZE_SOURCE("ANALYZE", ANALYZE_TBL.SOURCE),
	ANALYZE_DEST("ANALYZE", ANALYZE_TBL.DEST), 
	OPTIMIZE("OPTIMIZE", ANALYZE_TBL.ALL),
	OPTIMIZE_SOURCE("OPTIMIZE", ANALYZE_TBL.SOURCE), 
	OPTIMIZE_DEST("OPTIMIZE", ANALYZE_TBL.DEST),
	NONE("NONE", null);
	
	public String opt;
	public ANALYZE_TBL target;

	AnalyzeEnum(String opt, ANALYZE_TBL target) {
		this.opt = opt;
		this.target = target;
	}

	public static enum ANALYZE_TBL {
		ALL, SOURCE, DEST
	}
}
