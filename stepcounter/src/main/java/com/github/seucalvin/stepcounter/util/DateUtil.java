package com.github.seucalvin.stepcounter.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Calvin on 2021/7/21.
 * Desc:
 */
public class DateUtil {

    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<>();

    public static SimpleDateFormat getDateFormat() {
        SimpleDateFormat df = SIMPLE_DATE_FORMAT.get();
        if (df == null) {
            df = new SimpleDateFormat();
            SIMPLE_DATE_FORMAT.set(df);
        }
        return df;
    }

    public static String getCurrentDate() {
        return getCurrentDate(DEFAULT_DATE_PATTERN);
    }

    /**
     * 返回一定格式的当前时间
     */
    public static String getCurrentDate(String pattern) {
        getDateFormat().applyPattern(pattern);
        Date date = new Date(System.currentTimeMillis());
        return getDateFormat().format(date);
    }

    public static long getDateMillis(String dateString, String pattern) {
        long millionSeconds = 0;
        getDateFormat().applyPattern(pattern);
        try {
            millionSeconds = getDateFormat().parse(dateString).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millionSeconds;
    }

    /**
     * 格式化输入的millis
     */
    public static String dateFormat(long millis, String pattern) {
        getDateFormat().applyPattern(pattern);
        Date date = new Date(millis);
        String dateString = getDateFormat().format(date);
        return dateString;
    }

}
