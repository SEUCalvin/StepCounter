package com.github.seucalvin.stepcounter.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class StepModel implements Serializable, Parcelable {

    //当天时间，只显示到天 yyyy-MM-dd
    private String today;
    //步数记录时间戳
    private long date;
    //对应today的最大步数
    private long step;

    public StepModel() {

    }

    protected StepModel(Parcel in) {
        today = in.readString();
        date = in.readLong();
        step = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(today);
        dest.writeLong(date);
        dest.writeLong(step);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StepModel> CREATOR = new Creator<StepModel>() {
        @Override
        public StepModel createFromParcel(Parcel in) {
            return new StepModel(in);
        }

        @Override
        public StepModel[] newArray(int size) {
            return new StepModel[size];
        }
    };

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public String getToday() {
        return today;
    }

    public void setToday(String today) {
        this.today = today;
    }

    @Override
    public String toString() {
        return "StepModel{" +
                ", today=" + today +
                ", date=" + date +
                ", step=" + step +
                '}';
    }
}
