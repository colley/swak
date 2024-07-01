package com.swak.security.exception;

import com.swak.common.enums.IResultCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JwtTokenException extends AuthenticationException {

	public JwtTokenException(IResultCode resultCode) {
		super(resultCode.getMsg());
	}

	public JwtTokenException(String message) {
		super(message);
	}
}
