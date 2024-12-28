package com.example.courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class RunDatabase {

    //variables for database
    private SQLiteDatabase rdb;
    private Context context;
    private final RunDatabaseHelper dataHelper;

    public RunDatabase(Context c) {
        context = c;
        dataHelper = new RunDatabaseHelper(context);

    }


    public long insertData(String runDate, int runDistance, String runMessage, String runTime) {
        rdb = dataHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(RunConstants.DATE, runDate);
        contentValues.put(RunConstants.TIME, runTime);
        contentValues.put(RunConstants.DISTANCE, runDistance);
        contentValues.put(RunConstants.MESSAGE, runMessage);

        long id = rdb.insert(RunConstants.TABLE_NAME, null, contentValues);
        return id;
    }

    //gets all data from scheduled runs
    public Cursor getData() {
        SQLiteDatabase rdb = dataHelper.getWritableDatabase();   //get instance of database, inherited from SQLiteOpenHelper

        String[] columns = {RunConstants.DATE, RunConstants.TIME, RunConstants.DISTANCE, RunConstants.MESSAGE};
        Cursor cursor = rdb.query(RunConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
        return cursor;
    }

    //gets the dates of scheduled runs
    public Cursor getRunDates() {
        SQLiteDatabase rdb = dataHelper.getWritableDatabase();   //get instance of database, inherited from SQLiteOpenHelper

        String[] columns = {RunConstants.DATE};
        Cursor cursor = rdb.query(RunConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
        return cursor;
    }


    //delete run by date parameter
    public void deleteRun(String date) {
        rdb = dataHelper.getWritableDatabase();
        rdb.execSQL(" DELETE FROM " + RunConstants.TABLE_NAME + " WHERE " + RunConstants.DATE + "=\"" + date + "\";");

    }

}
