package com.github.seucalvin.stepcounter;

import android.Manifest;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.seucalvin.stepcounter.databinding.ActivityMainBinding;
import com.github.seucalvin.stepcounter.service.StepCounterService;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private ActivityMainBinding binding;

    private StepCounterInterface mStepCounterInterface;

    private final Handler mIntervalHandler = new Handler(new StepCounterCallback());

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REFRESH_STEP_FLAG = 1;
    private static final long TIME_INTERVAL_REFRESH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        askPermission();
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            initStepModule();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted 授予权限
                initStepModule();
            } else {
                binding.stepCount.setText("Permission denied");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartStepMessage();
    }

    /*初始化计步模块*/
    public void initStepModule() {
        binding.stepCount.setText("0");
        StepCounterService.init(getApplication(), this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mStepCounterInterface = StepCounterInterface.Stub.asInterface(service);
        try {
            if (mStepCounterInterface != null) {
                service.linkToDeath(() -> {
                    // 注册死亡代理, 当绑定的service异常断开连接后, 重新绑定service
                    if (mStepCounterInterface != null) {
                        initStepModule();
                    }
                }, 0);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        restartStepMessage();
    }

    private void restartStepMessage() {
        mIntervalHandler.removeMessages(REFRESH_STEP_FLAG);
        mIntervalHandler.sendEmptyMessageDelayed(REFRESH_STEP_FLAG, TIME_INTERVAL_REFRESH);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    private class StepCounterCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == REFRESH_STEP_FLAG) {
                if (mStepCounterInterface != null && mStepCounterInterface.asBinder().isBinderAlive()) {
                    int step = 0;
                    try {
                        step = mStepCounterInterface.getCurrentStepCounter();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    binding.stepCount.setText(String.valueOf(step));
                }
                restartStepMessage();
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIntervalHandler.removeMessages(REFRESH_STEP_FLAG);
    }
}