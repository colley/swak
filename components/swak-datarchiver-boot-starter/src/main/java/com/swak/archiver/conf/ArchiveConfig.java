
package com.swak.archiver.conf;

import com.swak.common.dto.base.DTO;
import lombok.Data;

/**
 * 数据归档和删除
 * @author colley.ma
 * @since 2023/2/2 17:45
 */
@Data
public class ArchiveConfig implements DTO {

	/** where条件*/
	private String where = "1=1";

	/** 每次limit取行数据归档处理）*/
	private Integer limit;

	/** 设置一个事务提交一次的数量. 单条插入和单条删除*/
	private Integer txnSize;

	/** 是否删除source数据库的相关匹配记录*/
	private boolean purge = Boolean.TRUE;

	/***是否归档 false=定时删除**/
	private boolean archive = Boolean.TRUE;

	/** progressSize每次归档限制总条数）*/
	private Integer progressSize = Integer.MAX_VALUE;

	/**尝试次数**/
	private Integer retries = 2;

	/** 每次归档了limit个行记录后的休眠1秒（单位为毫秒）*/
	private Long sleep = 5 * 1000L;

	/**归档的表名***/
	private String srcTblName;

	/**归档目标表名**/
	private String desTblName;

	/**是否clickhouse数据库**/
	private boolean clickhouseDb;

	/**
	 * clickhouse分区名
	 */
	private String  clusterName;

	/**clickhouse必填**/
	private String databaseName;

	/**是否批次执行**/
	private boolean bulk = true;
	
	/**是否执行表优化**/
	private AnalyzeEnum analyze;
	
	/**是否写binlog 添加NO_WRITE_TO_BINLOG参数**/
	private boolean local;

	/**是否发送邮件**/
	private boolean sendEmail;

	/**邮件接收人,逗号分隔**/
	private String recipients;
}
