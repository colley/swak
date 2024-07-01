package com.swak.archiver.executor.impl;


import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BatchDeleteExecutor extends AbsArchiveExecutor {

	@Override
	public void execute(ArchiveItem item) {
		TransactionTemplate transactionTemplate = item.getExecutor().getTransactionTemplate();
		// 需要循环递归，达到最大数 或则归档的条数为0
		while (item.getProgress().get() < item.getConfig().getProgressSize()) {
			try {
				int delRows = transactionTemplate.execute(new SwakTransactionCallback(item,null));
				if (delRows <= 0) {
					// 没有执行的记录数了
					break;
				}
			} catch (Exception e) {
				int retries = item.getRetries().incrementAndGet();
				log.error("执行删除报错，重试次数为  retries:{}",retries,e);
				if(item.getRetries().get()>=item.getConfig().getRetries()) {
					//超过重试次数
					break;
				}
				try {
					//500ms后重试
					TimeUnit.MILLISECONDS.sleep(500);
					continue;
				} catch (InterruptedException e1) {
					//igore
					log.error("{} ms后重试！", 500);
				}
			}
			try {
				TimeUnit.MILLISECONDS.sleep(item.getConfig().getSleep());
				log.warn("每次删除limit个行记录后休眠{} 毫秒", item.getConfig().getSleep());
			} catch (Exception e) {
				//igore
				//每次归档了limit个行记录后的休眠120秒（单位为秒）
				log.error("每次删除了limit个行记录后休眠报错！");
			}
		}
	}

	/**
	 * 根据max(id)和 limit 获取limit行数据处理 批量插入 同一个事务 
	 * replace INTO xxx_archive(`id`,`scene_id`) 
	 * SELECT !40001 SQL_NO_CACHE `id`, `scene_id` FROM xxx FORCE INDEX(`PRIMARY`) WHERE id<=1227 order by id LIMIT 10;
	 */
	@Override
	public int archiveItem(ArchiveItem item, List<Map<String, Object>> archiveData) {
		return 0;
	}

	/**
	 * 批量删除 DELETE FROM xxx WHERE id<=1227 order by id LIMIT 10;
	 */
	@Override
	public int deleteItem(ArchiveItem item,List<Map<String, Object>> archiveData) {
		ArchiveConfig config = item.getConfig();
		// 是否需要删除原表数据
		StringBuilder builderSql = new StringBuilder();
		builderSql.append("DELETE FROM ").append(config.getSrcTblName())
				.append(" WHERE id<=").append(item.getMaxId())
				.append(" order by id LIMIT ").append(config.getLimit());
		int rows = item.getExecutor().batchUpdate(builderSql.toString());
		return rows;
	}


}
