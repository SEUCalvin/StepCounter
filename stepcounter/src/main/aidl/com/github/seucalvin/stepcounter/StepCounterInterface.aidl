// StepCounterInterface.aidl
package com.github.seucalvin.stepcounter;

interface StepCounterInterface {

    /*获取当前时间运动步数*/
     int getCurrentStepCounter();

     /*获取当前设备是否支持步数计步器*/
     boolean getStepCounterSupported();

     /*获取数据库中所有的步数列表*/
     String getAllStepData();
}
