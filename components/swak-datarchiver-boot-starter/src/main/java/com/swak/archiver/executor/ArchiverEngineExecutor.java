
package com.swak.archiver.executor;


import com.google.common.collect.Sets;
import com.swak.archiver.ArchiveHandler;
import com.swak.archiver.common.IbsStringHelper;
import com.swak.archiver.common.SwakTemplateExecutor;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.archiver.conf.ArchiveLog;
import com.swak.archiver.notify.ArchiveLogMonitor;
import com.swak.archiver.notify.ArchiveMonitor;
import com.swak.common.chain.FilterChain;
import com.swak.common.chain.FilterChainFactory;
import com.swak.common.exception.ArchiveException;
import com.swak.common.util.GetterUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * ArchiverEngineExecutor.java
 *
 * @author ColleyMa
 * @version 19-5-13 下午2:35
 */
@Slf4j
public abstract  class ArchiverEngineExecutor implements ArchiverDataEngine {

	/** spring事务 */
	private SwakTemplateExecutor executor;
	private ArchiveMonitor monitor;


	protected abstract   List<ArchiveHandler> getArchiveHandler();


	public ArchiverEngineExecutor(SwakTemplateExecutor executor, ArchiveMonitor monitor) {
		this.executor = executor;
		this.monitor = monitor;
	}

	@Override
	public void engine(ArchiveConfig config) {
		ArchiveItem context = new ArchiveItem(config);
		context.setExecutor(executor);
		try {
			init(); // 初始化
			FilterChain<ArchiveItem> filterChain = FilterChainFactory.buildFilterChain(getArchiveHandler());
			filterChain.doFilter(context);
			done(context, null);
		} catch (Exception e) {
			log.error("数据归档失败！", e);
			done(context, e);
		}
	}

	public void init() throws ArchiveException {
		if (executor == null) {
			throw new ArchiveException("Property 'SwakTemplateExecutor' is required");
		}
	}

	public void done(ArchiveItem item, Exception ex) {
		if (monitor == null) {
			// 默认日志打印，可以自己实现发邮件等
			monitor = new ArchiveLogMonitor();
		}
		ArchiveLog.ArchiveLogBuilder builder = ArchiveLog.builder();
		if (ex == null) {
			builder.succ(true);
		} else {
			builder.succ(false);
		}

		builder.config(item.getConfig());
		// 计算剩余数
		Integer remainCount = 0;
		if(item.getConfig().isArchive()) {
			remainCount = GetterUtil.getInteger(item.getDataCount()) - item.getProgress().get();
		}else {
			remainCount = GetterUtil.getInteger(item.getDataCount()) - item.getDelNum().get();
		}
		builder.maxId(item.getMaxId()).limit(item.getConfig().getLimit()).progressSize(item.getProgress().get())
				.delTotalNum(item.getDelNum().get()).repeatNum(item.getRepeatNum().get())
				.retries(item.getRetries().get()).dataCount(item.getDataCount())
				.startTime(new Date(item.getStartTime())).endTime(new Date())
				.remainCount(remainCount > 0 ? remainCount : 0)// 剩余数量
				.costTime(IbsStringHelper.msTimeformat2String((System.currentTimeMillis() - item.getStartTime())));
		if (ex != null) {
			builder.traceInfo(ex.getMessage());
		}

		// 设置是否发邮件
		builder.sendEmail(item.getConfig().isSendEmail());
		// 设置接收人的邮件地址
		builder.recipients(Sets.newHashSet(GetterUtil.getSplit2List(item.getConfig().getRecipients())));
		monitor.monitor(builder.build());
	}

	@Override
	public void cancel() {
		
	}
}
