package com.github.seucalvin.stepcounter.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.seucalvin.stepcounter.model.StepModel;
import com.github.seucalvin.stepcounter.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Calvin on 2021/7/21.
 * Desc: 用来记录当天步数列表，传感器回调30次记录一条数据
 */
public class StepDBHelper extends SQLiteOpenHelper implements StepDBInterface {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "TodayStepDB.db";
    private static final String TABLE_NAME = "TodayStepData";
    private static final String PRIMARY_KEY = "_id";
    public static final String TODAY = "today";
    public static final String DATE = "date";
    public static final String STEP = "step";

    private static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + PRIMARY_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TODAY + " TEXT, "
            + DATE + " long, "
            + STEP + " long);";
    private static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    private static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME;
    private static final String SQL_QUERY_STEP_BY_DATE = "SELECT * FROM " + TABLE_NAME + " WHERE " + TODAY + " = ?";
    private static final String SQL_QUERY_STEP_ORDER_BY = "SELECT * FROM " + TABLE_NAME + " WHERE " + TODAY + " = ? ORDER BY " + STEP + " DESC";

    public static StepDBInterface factory(Context context) {
        return new StepDBHelper(context);
    }

    private StepDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteTable();
        onCreate(db);
    }

    @Override
    public synchronized void createTable() {
        getWritableDatabase().execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public synchronized void updateStep(StepModel stepModel) {
        Cursor cursor = getReadableDatabase().rawQuery(SQL_QUERY_STEP_BY_DATE, new String[]{stepModel.getToday()});
        List<StepModel> stepModelList = getTodayStepDataList(cursor);
        if (stepModelList.size() == 0) {
            // 今日首次记录, 直接插入
            ContentValues contentValues = new ContentValues();
            contentValues.put(TODAY, stepModel.getToday());
            contentValues.put(DATE, stepModel.getDate());
            contentValues.put(STEP, stepModel.getStep());
            getWritableDatabase().insert(TABLE_NAME, null, contentValues);
        } else {
            // 更新今日步数
            ContentValues newValues = new ContentValues();
            newValues.put(STEP, stepModel.getStep());
            getWritableDatabase().update(TABLE_NAME, newValues, "today = ?", new String[]{stepModel.getToday()});
        }
        cursor.close();
    }

    @Override
    public synchronized List<StepModel> getQueryAll() {
        Cursor cursor = getReadableDatabase().rawQuery(SQL_QUERY_ALL, new String[]{});
        List<StepModel> stepModelList = getTodayStepDataList(cursor);
        cursor.close();
        return stepModelList;
    }

    @Override
    public synchronized StepModel getStepByDate(long millis) {
        Cursor cursor = getReadableDatabase().rawQuery(SQL_QUERY_STEP_ORDER_BY, new String[]{DateUtil.dateFormat(millis, "yyyy-MM-dd")});
        StepModel stepModel = null;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            stepModel = getTodayStepData(cursor);
        }
        cursor.close();
        return stepModel;
    }

    private List<StepModel> getTodayStepDataList(Cursor cursor) {
        List<StepModel> stepModelList = new ArrayList<>();
        while (cursor.moveToNext()) {
            StepModel stepModel = getTodayStepData(cursor);
            stepModelList.add(stepModel);
        }
        return stepModelList;
    }

    private StepModel getTodayStepData(Cursor cursor) {
        String today = cursor.getString(cursor.getColumnIndex(TODAY));
        long date = cursor.getLong(cursor.getColumnIndex(DATE));
        long step = cursor.getLong(cursor.getColumnIndex(STEP));
        StepModel stepModel = new StepModel();
        stepModel.setToday(today);
        stepModel.setDate(date);
        stepModel.setStep(step);
        return stepModel;
    }

    @Override
    public synchronized void deleteTable() {
        getWritableDatabase().execSQL(SQL_DELETE_TABLE);
    }
}
