package com.github.seucalvin.stepcounter.util;

import java.util.Date;

/**
 * Created by Calvin on 2021/7/23.
 * Desc: 是否计步，23:55:50~00:05:50分不计步
 */
public class StepUtil {

    public static boolean isUploadStep() {

        Date date2355 = new Date(DateUtil.getDateMillis(
                DateUtil.getCurrentDate() + " 23:55:50", "yyyy-MM-dd HH:mm:ss"));

        Date date0005 = new Date(DateUtil.getDateMillis(
                DateUtil.getCurrentDate() + " 00:05:50", "yyyy-MM-dd HH:mm:ss"));

        Date currentDate = new Date(System.currentTimeMillis());
        if (currentDate.after(date2355)) {
            return false;
        }
        return !currentDate.before(date0005);
    }
}
