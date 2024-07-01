package com.swak.operatelog.annotation;

import java.util.Objects;

public enum LogScopeEnum {
    SUCCESS,//记录成功
    FAIL, //记录失败
    ALL; //全部记录

    public static boolean isALL(LogScopeEnum logType){
        return Objects.equals(LogScopeEnum.ALL,logType);
    }

    public static boolean isFail(LogScopeEnum logType){
        return Objects.equals(LogScopeEnum.FAIL,logType);
    }

    public static boolean isSuccess(LogScopeEnum logType){
        return Objects.equals(LogScopeEnum.SUCCESS,logType);
    }
}
