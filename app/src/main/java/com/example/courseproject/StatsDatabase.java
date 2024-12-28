package com.example.courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

//class to access and insert running stats data

public class StatsDatabase {

    //variables for database
    private SQLiteDatabase db;
    private Context context;
    private final StatsDatabaseHelper dataHelper;

    public StatsDatabase(Context c) {
        context = c;
        dataHelper = new StatsDatabaseHelper(context);
    }

    //inserting current/just finished run data
    public long insertRunData(int seconds, double km, double calories, int steps, double pace) {
        db = dataHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        //get current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = sdf.format(new Date());

        contentValues.put(StatsConstants.DATE, currentDate);
        contentValues.put(StatsConstants.KM, km);
        contentValues.put(StatsConstants.STEPS, steps);
        contentValues.put(StatsConstants.TIME, seconds);
        contentValues.put(StatsConstants.CALORIES, calories);
        contentValues.put(StatsConstants.PACE, pace);

        long id = db.insert(StatsConstants.TABLE_NAME, null, contentValues);
        return id;
    }

    //inserting past run data into database
    public long insertPastRunData(String date, int seconds, double km, double calories, int steps) {
        db = dataHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        double paceDouble;
        if (seconds != 0) { //make sure not dividing by 0
            double mins = seconds / 60;
            double hrs = mins / 60;

            paceDouble = km / hrs;
        } else {
            paceDouble = 0;
        }

        contentValues.put(StatsConstants.DATE, date);
        contentValues.put(StatsConstants.KM, km);
        contentValues.put(StatsConstants.STEPS, steps);
        contentValues.put(StatsConstants.TIME, seconds);
        contentValues.put(StatsConstants.CALORIES, calories);
        contentValues.put(StatsConstants.PACE, paceDouble);

        long id = db.insert(StatsConstants.TABLE_NAME, null, contentValues);
        return id;
    }

    //gets data for stats overview on dashboard
    public Cursor getRunDataDashboard() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();   //get instance of database, inherited from SQLiteOpenHelper

        String[] columns = {StatsConstants.DATE, StatsConstants.TIME, StatsConstants.KM, StatsConstants.STEPS, StatsConstants.CALORIES, StatsConstants.PACE};

        Cursor cursor = db.query(StatsConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
        return cursor;
    }


    //gets data for stats by run recyclerview on stats page
    public Cursor getRunDataStats() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();   //get instance of database, inherited from SQLiteOpenHelper

        String[] columns = {StatsConstants.DATE, StatsConstants.TIME, StatsConstants.KM, StatsConstants.STEPS, StatsConstants.CALORIES, StatsConstants.PACE, StatsConstants.UID};

        Cursor cursor = db.query(StatsConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
        return cursor;
    }

    //insert lat and lng doubles into map route table
    public long insertMapData(String longitude, String latitude, String tableName) {
        db = dataHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(StatsConstants.LAT, latitude);
        contentValues.put(StatsConstants.LNG, longitude);

        long id = db.insert("MapData" + tableName, null, contentValues);
        return id;
    }

    //gets data from map table to draw polyline on top
    public Cursor getMapData(String tableName) {
        SQLiteDatabase db = dataHelper.getWritableDatabase();   //get instance of database, inherited from SQLiteOpenHelper

        String[] columns = {StatsConstants.LAT, StatsConstants.LNG};

        Cursor cursor = null;
        try {
            cursor = db.query("MapData" + tableName, columns, null, null, null, null, null);
        } catch (Exception mapQueryError) { //catch if map table doesn't exist (keep cursor null)
        }
        return cursor;
    }

    //adds map table associated with stats database
    public String addMaptable() {
        String[] columns = {StatsConstants.UID};

        Cursor cursor = db.query(StatsConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
        while (!cursor.isLast()) { //why is this here?
            cursor.moveToNext();
        }
        String mapTableName = cursor.getString(0);
        dataHelper.addMapTable(db, mapTableName);
        return mapTableName;
    }


}