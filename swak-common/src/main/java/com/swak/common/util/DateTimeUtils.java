
package com.swak.common.util;

import com.swak.common.util.date.TemporalAccessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


/**
 * DateTimeUtils.java
 *
 * @author colley.ma
 * @since 2.4.0
 **/
@Slf4j
public final class DateTimeUtils {


    public static Date string2Date(String dateTimeStr, String datePattern) {
        if (StringUtils.isEmpty(dateTimeStr)) {
            return null;
        }
        try {
           return TemporalAccessorUtil.parse(dateTimeStr,datePattern);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }


    public static Date string2Date(String strTime) {
        int index = strTime.indexOf(":");
        String pattern =DatePattern.NORM_DATE_PATTERN;
        if (index != -1) {
            pattern = DatePattern.NORM_DATETIME_PATTERN;
        }
        return string2Date(strTime, pattern);
    }


    public static Calendar string2Calendar(String strtime) {
        Date d = string2Date(strtime);
        if (d != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        }
        return null;
    }

    public static Calendar string2Calendar(String strtime,String datePattern) {
        Date d = string2Date(strtime,datePattern);
        if (d != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        }
        return null;
    }

  
    public static Timestamp string2Timestamp(String strTime, String pattern) {
        Date date = string2Date(strTime, pattern);
        if (date != null) {
            return new Timestamp(date.getTime());
        }
        return null;
    }

    public static String calendar2String(Calendar time) {
        return date2String(time.getTime());
    }

    public static String calendar2String(Calendar time,String format) {
        return date2String(time.getTime(),format);
    }


    public static String date2String(Date time, String format) {
        if(Objects.isNull(time)){
            return null;
        }
        if (StringUtils.isEmpty(format)) {
            format = DatePattern.NORM_DATETIME_PATTERN;
        }
        return TemporalAccessorUtil.format(time.toInstant().atZone(ZoneId.systemDefault()),format);
    }

 
    public static String date2String(Timestamp time, String format) {
        if (StringUtils.isEmpty(format)) {
            format = DatePattern.NORM_DATETIME_PATTERN;
        }
        return TemporalAccessorUtil.format(time.toInstant().atZone(ZoneId.systemDefault()),format);
    }

  
    public static String date2String(Date time) {
        return date2String(time, null);
    }

   
    public static String date2String(Timestamp time) {
        return date2String(time, null);
    }

  
    public static String sysTimeStr(String format) {
        return date2String(new Date(), format);
    }

    public static String sysTimeStr() {
        return sysTimeStr(null);
    }

  
    public static String YYYYMMDD2String() {
        return YYYYMMDD2String(new Date());
    }

 
    public static String YYYYMMDD2String(Date date) {
        return date2String(date, DatePattern.YYYYMMDD_PATTERN);
    }

    public static String fullDate2String(Date date) {
        return date2String(date, DatePattern.NORM_DATETIME_PATTERN);
    }

   
    public static String formateShortDate(Date date) {
        return date2String(date, DatePattern.NORM_DATE_PATTERN);
    }

  
    public static String getShortDateStr() {
        return date2String(new Date(), DatePattern.NORM_DATE_PATTERN);
    }

 
    public static Date getLastTimeDay(Date date) {
        String dateTemp = date2String(date, DatePattern.NORM_DATE_PATTERN);
        return string2Date(dateTemp + " 23:59:59");
    }

    public static int weekOfYear(Date date, Integer gap) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.WEEK_OF_YEAR, gap);

        return Integer.valueOf(
                calendar.get(Calendar.YEAR) + String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR)));
    }

    public static Timestamp getCurrentTime() {
        return new Timestamp(System.currentTimeMillis());
    }


    public static Date resetTime2Zero(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
