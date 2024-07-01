package com.swak.archiver.spi;


import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.archiver.executor.ArchiveExecutor;
import com.swak.archiver.executor.impl.AloneArchiveExecutor;
import com.swak.archiver.executor.impl.BatchArchiveExecutor;
import com.swak.archiver.executor.impl.BatchDeleteExecutor;
import com.swak.common.chain.FilterInvoker;

public class ExecuteMysqlHandler implements ArchiveHandler {

	@Override
	public void doFilter(ArchiveItem context, FilterInvoker<ArchiveItem> nextFilter) {
		ArchiveConfig config = context.getConfig();
		//maxId 为null 没有数据需要归档
		if(context.getMaxId()==null) {
			nextFilter.invoke(context);
			return;
		}
		ArchiveExecutor executor= null;
		
		if(config.isArchive()) {
			//判断是批次还是单条
			if(config.isBulk()) {
				executor= new BatchArchiveExecutor();
			}else {
				executor = new AloneArchiveExecutor();
			}
		}else {
			executor = new BatchDeleteExecutor();
		}
		executor.execute(context);	
		nextFilter.invoke(context);
	}
}
