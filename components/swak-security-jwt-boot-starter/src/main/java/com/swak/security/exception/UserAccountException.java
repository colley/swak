package com.swak.security.exception;

import com.swak.common.enums.IResultCode;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserAccountException extends UsernameNotFoundException {

	private static final long serialVersionUID = -6198343293088168806L;

	private Integer code;

	private String msg;

	public UserAccountException(IResultCode resultCode) {
		super(resultCode.getMsg());
		this.code = resultCode.getCode();
		this.msg = resultCode.getMsg();
	}
	public UserAccountException(Integer code, String msg) {
		super(msg);
		this.code = code;
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

}
