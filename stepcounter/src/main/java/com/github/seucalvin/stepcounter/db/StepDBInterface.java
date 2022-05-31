package com.github.seucalvin.stepcounter.db;


import com.github.seucalvin.stepcounter.model.StepModel;

import java.util.List;

public interface StepDBInterface {

    void createTable();

    void deleteTable();

    // 更新今日步数
    void updateStep(StepModel stepModel);

    // 获取某日的最大步数
    StepModel getStepByDate(long millis);

    // 获取数据库所有数据, 用于调试
    List<StepModel> getQueryAll();
}
