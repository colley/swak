package com.swak.archiver.executor.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.common.dto.ListPager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AloneArchiveExecutor extends AbsArchiveExecutor {
	
	@Override
	public void execute(ArchiveItem item) {
		// 需要循环递归，达到最大数 或则归档的条数为0
		while (item.getProgress().get() < item.getConfig().getProgressSize()) {
			try {
				List<Map<String, Object>> archiveData = queryItems(item);
				if (CollectionUtils.isEmpty(archiveData)) {
					return;
				}
				int insertRows = doTransaction(item, archiveData);
				if (insertRows <= 0) {
					// 没有执行的记录数了
					return;
				}
				if (!item.getConfig().isPurge()) {
					super.handlerNextMaxId(item);
				}
			} catch (Exception e) {
				int retries = item.getRetries().incrementAndGet();
				log.error("execute  ArchiveItem error,retries:{}", retries, e);
				if (item.getRetries().get() >= item.getConfig().getRetries()) {
					// 超过重试次数
					return;
				}
				try {
					// 500ms后重试
					TimeUnit.MILLISECONDS.sleep(500);
					continue;
				} catch (InterruptedException e1) {
					// igore
				}
			}
			try {
				TimeUnit.MILLISECONDS.sleep(item.getConfig().getSleep());
				log.warn("每次归档了limit个行记录后休眠{} 毫秒", item.getConfig().getSleep());
			} catch (Exception e) {
				log.error("sleep error", e);
			}
		}

	}
	
	public int doTransaction(ArchiveItem item, List<Map<String, Object>> archivedata) {
		TransactionTemplate transactionTemplate = item.getExecutor().getTransactionTemplate();
		AtomicInteger executeCount = new AtomicInteger(0);
		// 分批执行
		int txnBacth = 1;
		ListPager<Map<String, Object>> listPager = new ListPager<Map<String, Object>>(archivedata,
				item.getConfig().getTxnSize());
		listPager.setPageIndex(txnBacth);
		List<Map<String, Object>> txndata = null;
		while (CollectionUtils.isNotEmpty(txndata = listPager.getPageList())) {
			int insertRows = transactionTemplate.execute(new SwakTransactionCallback(item, txndata));
			if (insertRows <= 0) {
				// 没有执行的记录数了
				return executeCount.get();
			}
			if(insertRows>txndata.size()) {
				executeCount.addAndGet(txndata.size());
			}else {
				executeCount.addAndGet(insertRows);
			}
			
			//重复条数
			item.getRepeatNum().addAndGet(insertRows-txndata.size());
			
			txnBacth++;
			listPager.setPageIndex(txnBacth);
		}
		return executeCount.get();
	}
	
	
	/**
	 * SELECT  `id`, `scene_id` FROM xxx FORCE INDEX(`PRIMARY`) WHERE id <=1227 order by id LIMIT 10;
	 * @param item
	 */
	protected List<Map<String, Object>> queryItems(ArchiveItem item){
		StringBuilder builderSql = new StringBuilder();
		builderSql.append("SELECT ")
				.append(Joiner.on(",").join(item.getCols()).toString())
				.append(" FROM ").append(item.getConfig().getSrcTblName() + " FORCE INDEX(`PRIMARY`)")
				.append(" WHERE id<=").append(item.getMaxId());

		//不删除原表数据，需要设置nextMaxId
		if(!item.getConfig().isPurge()) {
			builderSql.append(" and id>").append(item.getNextMaxId());
		}
		builderSql.append(" order by id LIMIT ").append(item.getConfig().getLimit());
		return item.getExecutor().getJdbcTemplate().queryForList(builderSql.toString());
	}

	/**
	 * replace INTO xxx_archive(`id`, `scene_id`) VALUES(?,?);
	 */
	@Override
	public int archiveItem(ArchiveItem item,List<Map<String, Object>> archiveData) {
		ArchiveConfig config = item.getConfig();
		 if (CollectionUtils.isEmpty(archiveData)) {
			 return 0;
		 }
		 List<String> questionMark = Lists.newArrayListWithExpectedSize(item.getCols().length);
		Arrays.stream(item.getCols()).forEach(a->{
			questionMark.add("?");
		});
		StringBuilder builderSql = new StringBuilder();
		builderSql.append("replace INTO ").append(config.getDesTblName()).append("(")
				.append(Joiner.on(",").join(item.getCols()).toString()).append(")")
				.append(" VALUES(").append(Joiner.on(",").join(questionMark).toString()).append(")");

		List<Object[]> batchArgs = Lists.newArrayListWithExpectedSize(archiveData.size());
		archiveData.stream().forEach(dataMap->{
			Object[] parameter = new Object[item.getCols().length];
			for(int i=0;i<item.getCols().length;i++) {
				String colName = item.getCols()[i];
				parameter[i] = dataMap.get(colName);
			}
			batchArgs.add(parameter);
		});
         int[] rowArrays = item.getExecutor().getJdbcTemplate().batchUpdate(builderSql.toString(),batchArgs);
		return rowArrays.length;
	}

	/**
	 * 批量删除 
	 * DELETE FROM xxx WHERE id<=1227 AND id=?
	 */
	@Override
	public int deleteItem(ArchiveItem item,List<Map<String, Object>> archivedata) {
		ArchiveConfig config = item.getConfig();
		String deleteSql = "DELETE FROM "+config.getSrcTblName()+" WHERE id<=? AND id=?";
		int deleteNum = 0;
		if(CollectionUtils.isNotEmpty(archivedata)) {
			List<Object[]> batchArgs = new ArrayList<Object[]>(archivedata.size());
			for (Iterator<Map<String, Object>> iterator = archivedata.iterator(); iterator.hasNext();) {
				Map<String, Object> data =  iterator.next();
				Long nextId = (Long) data.get("id");
				if(nextId!=null) {
					Object[] batchParameter = new Object[] {item.getMaxId(),nextId};
					batchArgs.add(batchParameter);
				}
			}
			if(CollectionUtils.isNotEmpty(batchArgs)) {
				int[] rowArrays = item.getExecutor().getJdbcTemplate().batchUpdate(deleteSql, batchArgs);
			    return rowArrays.length;
			}
		}
		return deleteNum;
	}
}
