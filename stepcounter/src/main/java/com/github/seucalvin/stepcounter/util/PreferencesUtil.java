package com.github.seucalvin.stepcounter.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by Calvin on 2021/7/21.
 * Desc:
 */
public class PreferencesUtil {

    public static final String APP_SHARD = "today_step_sp";

    // 上一次计步器的步数
    public static final String LAST_SENSOR_TIME = "last_sensor_time";
    // 步数补偿数值，每次传感器返回的步数-offset=当前步数
    public static final String STEP_OFFSET = "step_offset";
    // 当天，用来判断是否跨天
    public static final String STEP_TODAY = "step_today";
    // 清除步数
    public static final String CLEAN_STEP = "clean_step";
    // 当前步数
    public static final String CURR_STEP = "curr_step";
    //手机关机监听
    public static final String SHUTDOWN = "shutdown";
    //系统运行时间
    public static final String ELAPSED_REALTIME = "elapsed_realtime";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SHARD, Context.MODE_PRIVATE);
    }

    public static void setLastSensorStep(Context context, float lastSensorStep) {
        getSharedPreferences(context).edit().putFloat(LAST_SENSOR_TIME, lastSensorStep).apply();
    }

    public static float getLastSensorStep(Context context) {
        return getSharedPreferences(context).getFloat(LAST_SENSOR_TIME, 0.0f);
    }

    public static void setStepOffset(Context context, float stepOffset) {
        getSharedPreferences(context).edit().putFloat(STEP_OFFSET, stepOffset).apply();
    }

    public static float getStepOffset(Context context) {
        return getSharedPreferences(context).getFloat(STEP_OFFSET, 0.0f);
    }

    public static void setStepToday(Context context, String stepToday) {
        getSharedPreferences(context).edit().putString(STEP_TODAY, stepToday).apply();
    }

    public static String getStepToday(Context context) {
        return getSharedPreferences(context).getString(STEP_TODAY, "");
    }

    /**
     * true清除步数从0开始，false否
     */
    public static void setCleanStep(Context context, boolean cleanStep) {
        getSharedPreferences(context).edit().putBoolean(CLEAN_STEP, cleanStep).apply();
    }

    /**
     * true 清除步数，false否
     */
    public static boolean getCleanStep(Context context) {
        return getSharedPreferences(context).getBoolean(CLEAN_STEP, true);
    }

    public static void setCurrentStep(Context context, float currStep) {
        getSharedPreferences(context).edit().putFloat(CURR_STEP, currStep).apply();
    }

    public static float getCurrentStep(Context context) {
        return getSharedPreferences(context).getFloat(CURR_STEP, 0.0f);
    }

    public static void setElapsedRealtime(Context context, long elapsedRealtime) {
        getSharedPreferences(context).edit().putLong(ELAPSED_REALTIME, elapsedRealtime).apply();
    }

    public static long getElapsedRealtime(Context context) {
        return getSharedPreferences(context).getLong(ELAPSED_REALTIME, 0L);
    }

    public static void setShutdown(Context context, boolean shutdown) {
        getSharedPreferences(context).edit().putBoolean(SHUTDOWN, shutdown).apply();
    }

    public static boolean getShutdown(Context context) {
        return getSharedPreferences(context).getBoolean(SHUTDOWN, false);
    }
}
