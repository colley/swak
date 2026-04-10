package com.swak.security.exception;

import com.swak.common.enums.IResultCode;

public class JwtTokenException extends UserAccountException {

	public JwtTokenException(IResultCode resultCode,Throwable e) {
		super(resultCode,e);
	}
}
