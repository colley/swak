package com.swak.common.dto;

import com.swak.common.enums.IResultCode;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public class MessageResult<T> extends ResponseResult<T> implements MessageResp<T> {
    private String messageType;

    public MessageResult(IResultCode errCode) {
        super(errCode);
    }

    public MessageResult(Integer code, String msg) {
        super(code,msg);
    }

    @Override
    public String getMessageType() {
        return messageType;
    }

    @Override
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
