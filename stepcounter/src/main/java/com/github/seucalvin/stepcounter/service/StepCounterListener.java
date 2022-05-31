package com.github.seucalvin.stepcounter.service;

/**
 * Created by Calvin on 2021/7/21.
 * Desc:
 */
public interface StepCounterListener {

    /**
     * 用于显示步数
     */
    void onChangeStepCounter(int step);

    /**
     * 步数清零监听，跨越0点需要重新计步
     */
    void onStepCounterClean();

}
