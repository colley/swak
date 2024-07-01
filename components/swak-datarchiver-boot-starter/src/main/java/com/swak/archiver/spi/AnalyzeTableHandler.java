/**
 * Copyright (C), 2020-2021 by colley.ma
 *  
   *    表碎片优化
 * File Name: AnalyzeHandler.java
 * Encoding: UTF-8
 * Date: 2021年12月24日 下午2:24:36
 * History:
*/
package com.swak.archiver.spi;


import com.google.common.collect.Lists;
import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.AnalyzeEnum;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.chain.FilterInvoker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
public class AnalyzeTableHandler implements ArchiveHandler {
	@Override
	public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
		// 表碎片优化
		boolean retries = context.getRetries().get() >= context.getConfig().getRetries();
		if (retries) {
			log.error("异常超过重试最大数：{},不进行 analyze table优化！", context.getRetries().get());
			nextFilter.invoke(context);
			return;
		}

		ArchiveConfig config = context.getConfig();
		AnalyzeEnum analyzeEnum = config.getAnalyze();
		boolean isAnalyze = config.getAnalyze() != null && !Objects.equals(AnalyzeEnum.NONE, analyzeEnum);
		if (isAnalyze) {
			StringBuffer maintBuff = new StringBuffer();
			maintBuff.append(analyzeEnum.opt);
			if (config.isLocal()) {
				maintBuff.append("/*!40101 NO_WRITE_TO_BINLOG*/");
			}
			String maint = maintBuff.append(" TABLE ").toString();

			List<String> analyzeTableNames = Lists.newArrayListWithCapacity(2);
			switch (analyzeEnum.target) {
			case ALL:
				analyzeTableNames.add(config.getSrcTblName());
				analyzeTableNames.add(config.getDesTblName());
				break;
			case SOURCE:
				analyzeTableNames.add(config.getSrcTblName());
				break;

			case DEST:
				analyzeTableNames.add(config.getDesTblName());
				break;
			default:
				// ignore
				break;
			}
			// 执行表数据优化
			analyzeTable(context, maint, analyzeTableNames);
		}
		//执行下一个操作执行
		nextFilter.invoke(context);
	}

	private void analyzeTable(ArchiveItem context, String maint, List<String> tableNames) {
		if (CollectionUtils.isEmpty(tableNames)) {
			return;
		}
		for (String tableName : tableNames) {
			if (StringUtils.isEmpty(tableName)) {
				continue;
			}
			StringBuffer sql = new StringBuffer(maint).append(tableName);
			context.getExecutor().execute(sql.toString());
		}
	}
}
