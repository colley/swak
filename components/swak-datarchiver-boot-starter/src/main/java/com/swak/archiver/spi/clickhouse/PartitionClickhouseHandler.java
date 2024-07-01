
package com.swak.archiver.spi.clickhouse;


import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.chain.FilterInvoker;
import com.swak.common.exception.ArchiveException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 计算clickhouse数据是否需要删除和备份，根据时间分区来判断
 *
 * @author colley.ma
 * @since 2023/2/2 17:51
 */
@Slf4j
public class PartitionClickhouseHandler implements ArchiveHandler {

    @Override
	public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
    	ArchiveConfig config = context.getConfig();
        try {
			/**
			*SELECT partition FROM system.parts
			 * WHERE partition<xxx and database='fdc_dev' and table='summary_data_local' GROUP BY partition ORDER BY partition;
			*/
			StringBuilder builderSql = new StringBuilder();
			builderSql.append("SELECT partition FROM system.parts ");
			builderSql.append(" ").append(config.getWhere()).append(" and table='").append(config.getSrcTblName()+"'")
					.append(" and database='").append(config.getDatabaseName()).append("'")
					.append(" GROUP BY partition ORDER BY partition");
			List<String> partitionList = context.getExecutor().getJdbcTemplate().queryForList(builderSql.toString(),String.class);
			//设置归档数量
			context.setPartition(partitionList);
			//下一步
			nextFilter.invoke(context);
		} catch (Exception e) {
			log.error("获取clickhouse原数据表分区[partition]报错  - 原数据表 ：{} - where条件: {} - 异常信息： {}", config.getSrcTblName(),config.getWhere(), e.getMessage());
			throw new ArchiveException(e);
		}
	}
}
