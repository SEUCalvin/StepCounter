package com.github.seucalvin.stepcounter.util;

import android.content.Context;
import android.os.PowerManager;

import com.github.seucalvin.stepcounter.service.StepCounterService;

import java.util.Calendar;

/**
 * Created by Calvin on 2021/7/20.
 * Desc:
 */
public class WakeLockUtils {

    public static PowerManager.WakeLock mWakeLock;

    public synchronized static PowerManager.WakeLock getLock(Context context) {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, StepCounterService.class.getName());
        mWakeLock.setReferenceCounted(true);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        mWakeLock.acquire();
        return mWakeLock;
    }

}
