/**
 * Copyright (C), 2018 store
 * Encoding: UTF-8
 * Date: 19-5-27 下午8:59
 * History:
 */
package com.swak.archiver.conf;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * ArchiveLog.java
 * 
 * @author ColleyMa
 * @version 19-5-27 下午8:59
 */
@Data
@Builder
public class ArchiveLog {

	private ArchiveConfig config;

	/** 是否成功 */
	private boolean succ;

	/** 最大max(id) */
	private Long maxId;
	
	/** 总数据量 */
	private Integer dataCount;

	/** 剩余归档数 **/
	private Integer remainCount;

	/** Limit数 */
	private Integer limit;

	/** 归档总数 */
	private Integer progressSize;

	/** 删除总数 */
	private Integer delTotalNum;

	/** 归档重复总数 */
	private Integer repeatNum;

	/** 重试总次数 */
	private Integer retries;

	/** 总花费时间 */
	private String costTime;

	/** 异常信息 */
	private String traceInfo;

	/** 开始时间 **/
	private Date startTime;

	/** 结束时间 **/
	private Date endTime;

	private boolean sendEmail;

	private Set<String> recipients;

	protected List<String> partition;

}
