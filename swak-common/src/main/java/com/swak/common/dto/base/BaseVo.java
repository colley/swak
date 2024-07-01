package com.swak.common.dto.base;

import lombok.Data;

import java.util.Date;

@Data
public class BaseVo implements VO {

	private static final long serialVersionUID = -6992969104014044629L;

	private String creatorId;

	private Date createTime;

	private String modifierId;

	private Date modifyTime;
	
	private String creator;
	private String modifier;

	
}
