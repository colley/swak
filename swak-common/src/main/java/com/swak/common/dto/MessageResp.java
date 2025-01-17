package com.swak.common.dto;

import com.swak.common.enums.BasicErrCode;
import com.swak.common.enums.IResultCode;

/**
 * @author colley.ma
 * @since 3.0.0
 */
public interface MessageResp<T> extends Response<T> {

    String getMessageType();

    void setMessageType(String messageType);


    static <T> MessageResp<T> build(IResultCode errCode) {
        return new MessageResult<>(errCode);
    }
    static <T> MessageResp<T> build(T data,String messageType) {
        MessageResp<T> messageResp = new MessageResult<>(BasicErrCode.SUCCESS);
        messageResp.setMessageType(messageType);
        messageResp.setData(data);
        return messageResp;
    }
}
