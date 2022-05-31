package com.github.seucalvin.stepcounter.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;

import com.github.seucalvin.stepcounter.util.DateUtil;
import com.github.seucalvin.stepcounter.util.PreferencesUtil;
import com.github.seucalvin.stepcounter.util.WakeLockUtils;

/**
 * Created by Calvin on 2021/7/21.
 * Desc: 计步传感器计算当天步数，不需要后台Service
 */
public class StepSystemCounter implements SensorEventListener {

    private int mOffsetStep;
    private int mCurrentStep;
    private String mTodayDate;
    private boolean mCleanStep;
    private boolean mShutdown;

    //用来标识对象第一次创建
    private boolean mCounterReset = true;

    private final Context mContext;
    private final StepCounterListener mStepCounterListener;

    public StepSystemCounter(Context context, StepCounterListener listener) {
        this.mContext = context;
        this.mStepCounterListener = listener;

        WakeLockUtils.getLock(mContext);

        mCurrentStep = (int) PreferencesUtil.getCurrentStep(mContext);
        mCleanStep = PreferencesUtil.getCleanStep(mContext);
        mTodayDate = PreferencesUtil.getStepToday(mContext);
        mOffsetStep = (int) PreferencesUtil.getStepOffset(mContext);
        mShutdown = PreferencesUtil.getShutdown(mContext);

        //这里一定是关机开机了
        if (PreferencesUtil.getElapsedRealtime(mContext) > SystemClock.elapsedRealtime()) {
            mShutdown = true;
            PreferencesUtil.setShutdown(mContext, true);
        }

        initBroadcastReceiver();
        updateStepCounter();
    }

    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (Intent.ACTION_TIME_TICK.equals(intent.getAction())
                        || Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                    dateChangeCleanStep();
                }
            }
        };
        mContext.registerReceiver(mBatInfoReceiver, filter);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int stepCount = (int) event.values[0];
            if (mCleanStep) {
                cleanStep(stepCount);
            } else {
                //处理关机启动
                if (mShutdown || shutdownByCounterStep(stepCount)) {
                    shutdown(stepCount);
                }
            }
            mCurrentStep = stepCount - mOffsetStep;
            if (mCurrentStep < 0) {
                //任何原因步数小于0直接清零
                cleanStep(stepCount);
            }
            PreferencesUtil.setCurrentStep(mContext, mCurrentStep);
            PreferencesUtil.setElapsedRealtime(mContext, SystemClock.elapsedRealtime());
            PreferencesUtil.setLastSensorStep(mContext, stepCount);
            updateStepCounter();
        }
    }

    private void cleanStep(int counterStep) {
        //清除步数，步数归零，优先级最高
        mCurrentStep = 0;
        mOffsetStep = counterStep;
        PreferencesUtil.setStepOffset(mContext, mOffsetStep);

        mCleanStep = false;
        PreferencesUtil.setCleanStep(mContext, false);
    }

    private void shutdown(int counterStep) {
        int tmpCurrStep = (int) PreferencesUtil.getCurrentStep(mContext);
        //重新设置offset
        mOffsetStep = counterStep - tmpCurrStep;
        PreferencesUtil.setStepOffset(mContext, mOffsetStep);

        mShutdown = false;
        PreferencesUtil.setShutdown(mContext, false);
    }

    private boolean shutdownByCounterStep(int counterStep) {
        if (mCounterReset) {
            mCounterReset = false;
            return counterStep < PreferencesUtil.getLastSensorStep(mContext);
        }
        return false;
    }

    private synchronized void dateChangeCleanStep() {
        //时间改变了清零，或者0点分隔回调
        if (!getTodayDate().equals(mTodayDate)) {
            WakeLockUtils.getLock(mContext);

            mCleanStep = true;
            PreferencesUtil.setCleanStep(mContext, true);

            mTodayDate = getTodayDate();
            PreferencesUtil.setStepToday(mContext, mTodayDate);

            mShutdown = false;
            PreferencesUtil.setShutdown(mContext, false);

            mCurrentStep = 0;
            PreferencesUtil.setCurrentStep(mContext, mCurrentStep);

            if (mStepCounterListener != null) {
                mStepCounterListener.onStepCounterClean();
            }
        }
    }

    private String getTodayDate() {
        return DateUtil.getCurrentDate();
    }

    private void updateStepCounter() {
        //每次回调都判断一下是否跨天
        dateChangeCleanStep();
        if (mStepCounterListener != null) {
            mStepCounterListener.onChangeStepCounter(mCurrentStep);
        }
    }

    public int getCurrentStep() {
        mCurrentStep = (int) PreferencesUtil.getCurrentStep(mContext);
        return mCurrentStep;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
