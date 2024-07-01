package com.swak.operatelog.annotation;

import com.swak.common.util.MapBean;

public class OperateLogContext extends MapBean {

    private String USER_ID = "USER_ID";

    public void setUserId(String userId) {
        super.put(USER_ID, userId);
    }

    public String getUserId() {
        return super.getString(USER_ID);
    }
}
