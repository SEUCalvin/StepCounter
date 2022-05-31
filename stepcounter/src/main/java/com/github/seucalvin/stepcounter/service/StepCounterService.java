package com.github.seucalvin.stepcounter.service;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.github.seucalvin.stepcounter.R;
import com.github.seucalvin.stepcounter.StepCounterInterface;
import com.github.seucalvin.stepcounter.db.StepDBHelper;
import com.github.seucalvin.stepcounter.db.StepDBInterface;
import com.github.seucalvin.stepcounter.model.StepModel;
import com.github.seucalvin.stepcounter.receiver.ReceiverUtil;
import com.github.seucalvin.stepcounter.util.DateUtil;
import com.github.seucalvin.stepcounter.util.NotificationUtil;
import com.github.seucalvin.stepcounter.util.StepJsonUtil;
import com.github.seucalvin.stepcounter.util.StepUtil;
import com.github.seucalvin.stepcounter.util.WakeLockUtils;

import org.json.JSONArray;

import java.util.List;


/**
 * Created by Calvin on 2021/7/21.
 */
public class StepCounterService extends Service implements
        Handler.Callback, StepCounterListener {

    private static final String STEP_CHANNEL_ID = "stepChannelId";

    /**
     * 步数通知ID
     */
    private static final int NOTIFY_ID = 1000;

    /**
     * 保存数据库频率, 子进程存活的情况下约 200 * 3s = 10min
     */
    private static final int DB_SAVE_COUNTER = 200;
    /**
     * 刷新通知栏步数
     */
    private static final int WHAT_REFRESH_NOTIFY_STEP = 1;

    /**
     * 刷新通知栏步数，3s一次
     */
    private static final int REFRESH_NOTIFY_STEP_DURATION = 3 * 1000;

    /**
     * 点击通知栏广播requestCode
     */
    private static final int BROADCAST_REQUEST_CODE = 100;

    private static int CURRENT_STEP = 0;

    private SensorManager mSensorManager;
    private StepSystemAccelerometer mStepAccelerometer;
    private StepSystemCounter mStepCounter;

    private NotificationUtil mNotification;
    private int mDbSaveCount = 0;
    private StepDBInterface mStepDBHelper;

    public static void init(Application application, ServiceConnection serviceConnection) {
        try {
            Intent intent = new Intent(application, StepCounterService.class);
            ContextCompat.startForegroundService(application, intent);
            application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler mHandler = new Handler(this);

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == WHAT_REFRESH_NOTIFY_STEP) {
            // 刷新通知栏并记录数据库
            updateNotification();
            saveStepToDB(true);
            restartStepMessage();
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStepDBHelper = StepDBHelper.factory(getApplicationContext());
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initNotification(CURRENT_STEP);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDbSaveCount = 0;
        updateNotification();
        startStepDetector();
        saveStepToDB(false);
        restartStepMessage();
        return START_STICKY;
    }

    private void restartStepMessage() {
        mHandler.removeMessages(WHAT_REFRESH_NOTIFY_STEP);
        mHandler.sendEmptyMessageDelayed(WHAT_REFRESH_NOTIFY_STEP, REFRESH_NOTIFY_STEP_DURATION);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mIBinder.asBinder();
    }

    private synchronized void initNotification(int currentStep) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String receiverName = ReceiverUtil.getReceiver(getApplicationContext());
        PendingIntent contentIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE, new Intent(), getPendingIntentFlag());
        if (!TextUtils.isEmpty(receiverName)) {
            try {
                contentIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE, new Intent(this, Class.forName(receiverName)), getPendingIntentFlag());
            } catch (Exception e) {
                e.printStackTrace();
                contentIntent = PendingIntent.getBroadcast(this, BROADCAST_REQUEST_CODE, new Intent(), getPendingIntentFlag());
            }
        }
        String km = StepJsonUtil.getDistanceByStep(currentStep);
        String calorie = StepJsonUtil.getCalorieByStep(currentStep);
        String contentText = "走路约" + km + "公里, 消耗约" + calorie + "千卡";
        mNotification = new NotificationUtil.Builder(this, notificationManager, STEP_CHANNEL_ID,
                getString(R.string.step_channel_name), R.mipmap.ic_launcher)
                .setContentIntent(contentIntent)
                .setContentText(contentText)
                .setContentTitle(getString(R.string.title_notification_bar, String.valueOf(currentStep)))
                .setTicker(getString(R.string.step_channel_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setOnlyAlertOnce(true)
                .builder();
        mNotification.startForeground(this, NOTIFY_ID);
        mNotification.notify(NOTIFY_ID);
    }

    private int getPendingIntentFlag() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    private void startStepDetector() {
        //android4.4一定是满足的, 但是不一定有stepCounter
        if (!stepCounterSupported()) {
            addStepDetectorListener();
        } else {
            //大多数手机使用系统自带的stepCounter
            addStepCounterListener();
        }
    }

    private void addStepCounterListener() {
        if (mStepCounter != null) {
            WakeLockUtils.getLock(this);
            CURRENT_STEP = mStepCounter.getCurrentStep();
            updateNotification();
            return;
        }
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor == null) {
            return;
        }
        mStepCounter = new StepSystemCounter(getApplicationContext(), this);
        CURRENT_STEP = mStepCounter.getCurrentStep();
        mSensorManager.registerListener(mStepCounter, countSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void addStepDetectorListener() {
        if (mStepAccelerometer != null) {
            WakeLockUtils.getLock(this);
            CURRENT_STEP = mStepAccelerometer.getCurrentStep();
            updateNotification();
            return;
        }

        // 加速度传感器
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null) {
            return;
        }
        mStepAccelerometer = new StepSystemAccelerometer(this, this);
        CURRENT_STEP = mStepAccelerometer.getCurrentStep();
        mSensorManager.registerListener(mStepAccelerometer, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void saveStepToDB(boolean needCount) {
        if (needCount && mDbSaveCount < DB_SAVE_COUNTER) {
            mDbSaveCount++;
            return;
        }

        mDbSaveCount = 0;
        StepModel stepModel = new StepModel();
        stepModel.setToday(DateUtil.getCurrentDate());
        stepModel.setDate(System.currentTimeMillis());
        stepModel.setStep(CURRENT_STEP);
        if (mStepDBHelper != null) {
            mStepDBHelper.updateStep(stepModel);
        }
    }

    private void cleanDB() {
        mDbSaveCount = 0;
        if (mStepDBHelper != null) {
            mStepDBHelper.deleteTable();
            mStepDBHelper.createTable();
        }
    }

    private synchronized void updateNotification() {
        if (mNotification != null) {
            String km = StepJsonUtil.getDistanceByStep(CURRENT_STEP);
            String calorie = StepJsonUtil.getCalorieByStep(CURRENT_STEP);
            String contentText = "走路约" + km + "公里, 消耗约" + calorie + "千卡";
            mNotification.updateNotification(NOTIFY_ID, getString(R.string.title_notification_bar,
                    String.valueOf(CURRENT_STEP)), contentText);
        }
    }

    private boolean stepCounterSupported() {
        Sensor countSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        return countSensor != null;
    }

    @Override
    public void onChangeStepCounter(int step) {
        if (StepUtil.isUploadStep()) {
            CURRENT_STEP = step;
        }
    }

    @Override
    public void onStepCounterClean() {
        CURRENT_STEP = 0;
        updateNotification();
    }

    private final StepCounterInterface.Stub mIBinder = new StepCounterInterface.Stub() {
        @Override
        public int getCurrentStepCounter() throws RemoteException {
            return CURRENT_STEP;
        }

        @Override
        public boolean getStepCounterSupported() throws RemoteException {
            return stepCounterSupported();
        }

        @Override
        public String getAllStepData() throws RemoteException {
            if (mStepDBHelper != null) {
                List<StepModel> stepModelArrayList = mStepDBHelper.getQueryAll();
                JSONArray jsonArray = getSportStepJsonArray(stepModelArrayList);
                return jsonArray.toString();
            }
            return null;
        }

        private JSONArray getSportStepJsonArray(List<StepModel> stepModelArrayList) {
            return StepJsonUtil.getSportStepJsonArray(stepModelArrayList);
        }
    };
}
