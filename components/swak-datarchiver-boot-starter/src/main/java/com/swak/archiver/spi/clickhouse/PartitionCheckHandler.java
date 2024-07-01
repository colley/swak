
package com.swak.archiver.spi.clickhouse;


import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.chain.FilterInvoker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

/**
 * 判断clickhouse是否需要
 *
 * @author colley.ma
 * @since 2023/2/2 17:51
 */
@Slf4j
public class PartitionCheckHandler implements ArchiveHandler {
    @Override
	public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
		ArchiveConfig config = context.getConfig();
		if (CollectionUtils.isEmpty(context.getPartition())) {
			log.warn("原始表数获取 - 原始表: {} - 需要处理数分区为空- where条件: {}",new Object[]{config.getSrcTblName(), config.getWhere()});
		}
		nextFilter.invoke(context);
	}
}
