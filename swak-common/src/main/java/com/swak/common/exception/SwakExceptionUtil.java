package com.swak.common.exception;


import com.swak.common.enums.IResultCode;
import com.swak.common.util.StringPool;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SwakExceptionUtil {
    public static void throwException(String e) throws BizException {
        throw new BizException(e);
    }

    public static void throwException(Throwable e) throws BizException {
        throw new BizException(e);
    }

    public static void throwException(String er, Throwable e) throws BizException {
        throw new BizException(er, e);
    }

    public static void throwException(IResultCode resultCode) throws BizException {
        throw new BizException(resultCode);
    }

    public static void throwException(IResultCode resultCode, Object... args) throws BizException {
        throw new BizException(resultCode, args);
    }

    public static void throwException(IResultCode resultCode, String errMessage, Object... args) throws BizException {
        throw new BizException(resultCode, errMessage, args);
    }


    public static void throwException(IResultCode resultCode, Throwable e) throws BizException {
        throw new BizException(resultCode, resultCode.getI18nMsg(), e);
    }

    public static void throwException(IResultCode resultCode, String er) throws BizException {
        throw new BizException(resultCode, er);
    }

    public static void throwException(IResultCode resultCode, String er, Throwable e) throws BizException {
        throw new BizException(resultCode, er, e);
    }

    public static void throwException(Integer retCode, String er) throws BizException {
        throw new BizException(retCode, er);
    }

    public static String getFirstStackTrace(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (ArrayUtils.isEmpty(stackTrace)) {
            return e.getMessage();
        }
        StackTraceElement first = stackTrace[0];
        if (Objects.nonNull(first)) {
            return first.toString();
        }
        return StringPool.EMPTY;
    }


    public static String getExceptionStackTrace(Throwable e) {
        StringBuffer sb = new StringBuffer();
        sb.append(e);

        StackTraceElement[] trace = e.getStackTrace();

        for (int i = 0; i < trace.length; i++) {
            sb.append("\n " + trace[i]);
        }

        Throwable ourCause = e.getCause();

        if (ourCause != null) {
            getCauseStackTrace(sb, ourCause);
        }

        return sb.toString();
    }


    public static Throwable getRootCase(Throwable a) {
        Throwable r = null;

        for (r = a; r.getCause() != null; r = r.getCause()) {
            ;
        }

        return r;
    }

    private static void getCauseStackTrace(StringBuffer sb, Throwable cause) {
        if (null != cause) {
            StackTraceElement[] trace = cause.getStackTrace();

            for (int i = 0; i < trace.length; i++) {
                sb.append("\n " + trace[i]);
            }

            Throwable ourCause = cause.getCause();

            if (ourCause != null) {
                getCauseStackTrace(sb, ourCause);
            }
        }
    }


    private static void getCauseStackTrace(List<String> list, Throwable cause) {
        if (null != cause) {
            list.add(cause.getClass().getName());
            getCauseStackTrace(list, cause.getCause());
        }
    }

    public static List<String> getCauseExceptionName(Throwable cause) {
        List<String> list = new ArrayList<String>();
        getCauseStackTrace(list, cause);
        return list;
    }


    public static boolean isContains(Throwable cause, String exceptionName) {
        List<String> exceptionNameList = getCauseExceptionName(cause);
        return exceptionNameList.contains(exceptionName);
    }


}
