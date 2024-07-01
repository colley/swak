package com.swak.archiver.spi.clickhouse;


import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.archiver.executor.ArchiveExecutor;
import com.swak.archiver.executor.impl.ArchiveClickhouseExecutor;
import com.swak.common.chain.FilterInvoker;
import org.apache.commons.collections4.CollectionUtils;

public class ExecuteClickHandler implements ArchiveHandler {
	private ArchiveExecutor executor;

	public ExecuteClickHandler() {
		this.executor = new ArchiveClickhouseExecutor();
	}
	@Override
	public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
		ArchiveConfig config = context.getConfig();
		if (CollectionUtils.isEmpty(context.getPartition())) {
			nextFilter.invoke(context);
			return;
		}
		if(!config.isArchive()){
			//删除历史数据
			executor.execute(context);
		}
		nextFilter.invoke(context);
	}
}
