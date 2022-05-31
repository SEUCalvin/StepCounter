package com.github.seucalvin.stepcounter.receiver;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Calvin on 2021/7/21.
 * Desc:
 */
public class ReceiverUtil {

    public static String getReceiver(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS);
            ActivityInfo[] activityInfoList = packageInfo.receivers;
            if (activityInfoList != null && activityInfoList.length > 0) {
                for (ActivityInfo activityInfo : activityInfoList) {
                    String receiverName = activityInfo.name;
                    Class superClazz = Class.forName(receiverName).getSuperclass();
                    int count = 1;
                    while (superClazz != null) {
                        if (superClazz.getName().equals("java.lang.Object")) {
                            break;
                        }
                        if (superClazz.getName().equals(BaseClickBroadcast.class.getName())) {
                            return receiverName;
                        }
                        if (count > 20) {//防止死循环
                            break;
                        }
                        count++;
                        superClazz = superClazz.getSuperclass();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
