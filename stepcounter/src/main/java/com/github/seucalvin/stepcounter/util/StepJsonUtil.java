package com.github.seucalvin.stepcounter.util;

import com.github.seucalvin.stepcounter.model.StepModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class StepJsonUtil {

    public static final String SPORT_DATE = "sportDate";
    public static final String STEP_NUM = "stepNum";
    public static final String DISTANCE = "km";
    public static final String CALORIE = "kaluli";
    public static final String TODAY = "today";

    public static JSONArray getSportStepJsonArray(List<StepModel> stepModelArrayList) {
        JSONArray jsonArray = new JSONArray();
        if (null == stepModelArrayList || 0 == stepModelArrayList.size()) {
            return jsonArray;
        }
        for (int i = 0; i < stepModelArrayList.size(); i++) {
            StepModel stepModel = stepModelArrayList.get(i);
            try {
                JSONObject subObject = getJSONObject(stepModel);
                jsonArray.put(subObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    static JSONObject getJSONObject(StepModel stepModel) throws JSONException {
        JSONObject subObject = new JSONObject();
        subObject.put(TODAY, stepModel.getToday());
        subObject.put(SPORT_DATE, stepModel.getDate() / 1000);
        subObject.put(STEP_NUM, stepModel.getStep());
        subObject.put(DISTANCE, getDistanceByStep(stepModel.getStep()));
        subObject.put(CALORIE, getCalorieByStep(stepModel.getStep()));
        return subObject;
    }

    // 公里计算公式
    public static String getDistanceByStep(long steps) {
        return String.format("%.2f", steps * 0.6f / 1000);
    }

    // 千卡路里计算公式
    public static String getCalorieByStep(long steps) {
        return String.format("%.1f", steps * 0.6f * 60 * 1.036f / 1000);
    }

}
