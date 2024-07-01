
package com.swak.archiver.spi;


import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.chain.FilterInvoker;
import com.swak.common.exception.ArchiveException;
import lombok.extern.slf4j.Slf4j;


/**
 * 	计算是否有数据需要归档，count<=0 不处理数据
 * 	CountHandler.java
 * 	@author ColleyMa
 * 	@version 19-5-7 下午4:20
 */

@Slf4j
public class CountHandler implements ArchiveHandler {

	@Override
	public  void  doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
		ArchiveConfig config = context.getConfig();
		try {
			/**
			 * SELECT COUNT(1) FROM xxx  where gmt_create<"2020-08-10";
			 */
			StringBuilder builderSql = new StringBuilder();
			builderSql.append("SELECT COUNT(1) FROM ").append(config.getSrcTblName());
			builderSql.append(" ").append(config.getWhere());
			Integer dataCount = context.getExecutor().findTotalCount(builderSql.toString());
			//设置归档数量
			context.setDataCount(dataCount);
			//下一步
			nextFilter.invoke(context);

		} catch (Exception e) {
			log.error("获取原数据表maxId报错  - 原数据表 ：{} - where条件: {} - 异常信息： {}", config.getSrcTblName(),config.getWhere(), e.getMessage());
			throw new ArchiveException(e);
		}
	}
}
