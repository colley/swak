
package com.swak.archiver.executor.impl;


import com.swak.archiver.common.IbsStringHelper;
import com.swak.archiver.conf.ArchiveConfig;
import com.swak.archiver.conf.ArchiveItem;
import com.swak.archiver.executor.ArchiveExecutor;
import com.swak.common.exception.ArchiveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import java.util.List;
import java.util.Map;

/**
 * AbsArchiveExecutor.java
 *
 * @author ColleyMa
 * @version 19-5-10 上午11:14
 */
@Slf4j
public abstract class AbsArchiveExecutor implements ArchiveExecutor {

	/**
	 * 获取归档目标表的最大maxId
	 *
	 * @param item
	 */
	protected void handlerNextMaxId(ArchiveItem item) {
		ArchiveConfig config = item.getConfig();

		try {
			/** SELECT MAX(id) FROM xxx_archive; */
			StringBuilder builderSql = new StringBuilder();
			builderSql.append("SELECT MAX(id) FROM ").append(config.getDesTblName());
			Long maxId = item.getExecutor().findMaxId(builderSql.toString());
			log.warn("目标表nextMaxId获取 - 目标表: {} - nextMaxId(id) :{}", new Object[] { config.getDesTblName(), maxId });
			// 设置最大的MaxId
			item.setNextMaxId(maxId);
		} catch (Exception e) {
			log.error("目标表nextMaxId获取 - 目标表: {}  - 异常信息： {}", config.getDesTblName(), e.getMessage());
			ArchiveException.throwException(e);
		}
	}

	public void commitArchiveLog(ArchiveItem item, int insertNum, int deleteNum, boolean isSucc) {
		StringBuffer strBuff = new StringBuffer();

		if (isSucc) {
			strBuff.append("commit成功  ");
		} else {
			strBuff.append("执行失败回滚   ");
		}

		strBuff.append(" - 当前归档表 : ").append(item.getConfig().getSrcTblName());
		strBuff.append(" - 目标表 : ").append(item.getConfig().getDesTblName());
		strBuff.append(" - 最大max(id) : ").append(item.getMaxId());
		strBuff.append(" - Limit数 : ").append(item.getConfig().getLimit());
		strBuff.append(" - txn-size : ").append(item.getConfig().getTxnSize());
		strBuff.append(" - 归档行数 : ").append(insertNum);
		strBuff.append(" - 归档重复行数 : ").append(item.getRepeatNum().get());
		strBuff.append(" - 删除行数: ").append(deleteNum);
		strBuff.append(" - 重试总次数 : ").append(item.getRetries().get());
		strBuff.append(" - 花费时间: ")
				.append(IbsStringHelper.msTimeformat2String((System.currentTimeMillis() - item.getStartTime())));
		log.warn(strBuff.toString());
	}

	public void commitDelLog(ArchiveItem item, int deleteNum, boolean isSucc) {
		StringBuffer strBuff = new StringBuffer();

		if (isSucc) {
			strBuff.append("commit成功  ");
		} else {
			strBuff.append("执行失败回滚   ");
		}

		strBuff.append(" - 当前表 : ").append(item.getConfig().getSrcTblName());
		strBuff.append(" - 最大max(id) : ").append(item.getMaxId());
		strBuff.append(" - Limit数 : ").append(item.getConfig().getLimit());
		strBuff.append(" - txn-size : ").append(item.getConfig().getTxnSize());
		strBuff.append(" - 删除行数: ").append(deleteNum);
		strBuff.append(" - 删除总行数: ").append(item.getDelNum().get());
		strBuff.append(" - 剩余总行数: ").append(item.getConfig().getProgressSize()-item.getDelNum().get());
		strBuff.append(" - 重试总次数 : ").append(item.getRetries().get());
		strBuff.append(" - 花费时间: ")
				.append(IbsStringHelper.msTimeformat2String((System.currentTimeMillis() - item.getStartTime())));
		log.warn(strBuff.toString());
	}

	public class SwakTransactionCallback implements TransactionCallback<Integer> {
		private ArchiveItem item;
		private List<Map<String, Object>> archiveData;

		public SwakTransactionCallback(ArchiveItem item, List<Map<String, Object>> archiveData) {
			this.item = item;
			this.archiveData = archiveData;
		}

		@Override
		public Integer doInTransaction(TransactionStatus status) {
			ArchiveConfig config = item.getConfig();

			if (!config.isArchive()) {
				return doDelTransaction(status);
			}
			return doArchiveTransaction(status);

		}

		//归档
		private Integer doArchiveTransaction(TransactionStatus status) {
			int insertNum = 0;
			int deleteNum = 0;
			ArchiveConfig config = item.getConfig();
			try {
				// 先归档
				insertNum = archiveItem(item, archiveData);
				// 归档成功才能删除
				if (insertNum > 0) {
					// 执行删除操作
					if (config.isPurge()) {
						deleteNum = deleteItem(item, archiveData);
						item.getDelNum().addAndGet(deleteNum);
					}

					if (insertNum > item.getConfig().getLimit()) {
						// 设置插入的条数
						item.getProgress().addAndGet(item.getConfig().getLimit());
					} else {
						// 设置插入的条数
						item.getProgress().addAndGet(insertNum);
					}

					// 重复条数
					item.getRepeatNum().addAndGet(insertNum - deleteNum);
				}
			} catch (Exception e) {
				log.error("===> 批次归档失败，执行回滚操作", e);
				commitArchiveLog(item, insertNum, deleteNum, false);
				// 设置回滚
				status.setRollbackOnly();
				throw new RuntimeException("批次归档失败，执行回滚操作", e);
			}

			commitArchiveLog(item, insertNum, deleteNum, true);

			return insertNum;
		}

		//删除历史数据
		private Integer doDelTransaction(TransactionStatus status) {
			int deleteNum = 0;
			try {
				deleteNum = deleteItem(item, archiveData);
				item.getDelNum().addAndGet(deleteNum);
				item.getProgress().addAndGet(deleteNum);
			} catch (Exception e) {
				log.error("===> 批次归档失败，执行回滚操作", e);
				commitDelLog(item, deleteNum, false);
				// 设置回滚
				status.setRollbackOnly();
				throw new RuntimeException("批次归档失败，执行回滚操作", e);
			}

			commitDelLog(item, deleteNum, true);

			return deleteNum;
		}
	}
}
