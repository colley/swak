package com.swak.archiver.spi;

import com.google.common.collect.Sets;
import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.chain.FilterInvoker;
import com.swak.common.exception.ArchiveException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查原表和目标表字段是否一致 TableCheckHandler.java
 * 
 * @author ColleyMa
 * @version 19-5-7 下午4:20
 */
@Slf4j
public class TableCheckHandler implements ArchiveHandler {

	@Override
	public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
		ArchiveConfig config = context.getConfig();
		if (!config.isArchive()) {
			log.warn("删除历史数据不跳过check字段一致性， tableName：{}", config.getSrcTblName());
			nextFilter.invoke(context);
			return;
		}
		// 走归档逻辑
		List<String> srcCols = getTableCols(context, config.getSrcTblName());
		List<String> desCols = getTableCols(context, config.getDesTblName());
		if (!ArrayUtils.toString(Sets.newTreeSet(srcCols)).equals(ArrayUtils.toString(Sets.newTreeSet(desCols)))) {
			// 归档的表结构不一致
			ArchiveException.throwException("原始表：{},与目标表:{} 字段不一致", config.getSrcTblName(), config.getDesTblName());
		}
		// 设置归档表字段
		context.setCols(srcCols.toArray(new String[srcCols.size()]));
		nextFilter.invoke(context);
	}

	protected List<String> getTableCols(ArchiveItem item, String tblName) {
		List<String> colsName = new ArrayList<String>();
		try {
			StringBuilder builderSql = new StringBuilder();
			builderSql.append("SELECT * FROM ").append(tblName).append(" limit 0,0 ");
			SqlRowSet rowSet = item.getExecutor().queryForRowSet(builderSql.toString());
			SqlRowSetMetaData metaData = rowSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				colsName.add(metaData.getColumnName(i));
			}
		} catch (DataAccessException e) {
			log.error("获取表字段报错  tableName：{}", tblName, e);
			ArchiveException.throwException(e);
		}
		return colsName;
	}

}
