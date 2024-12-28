package com.example.courseproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

  //class to access and insert journal entries into journal database

public class JournalDatabase {

    //variables for database
    private SQLiteDatabase db;
    private Context context;
    private final JournalDatabaseHelper dataHelper;

    public JournalDatabase (Context c){
        context = c;
        dataHelper = new JournalDatabaseHelper(context);
    }

    //insert date and entry
    public long insertData (String date, String entry)
    {
        db = dataHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(JournalConstants.DATE, date);
        contentValues.put(JournalConstants.ENTRY, entry);
        long id = db.insert(JournalConstants.TABLE_NAME, null, contentValues);
        return id;
    }

    //gets the journal data (date and entry)
    public Cursor getData()
    {
        SQLiteDatabase db = dataHelper.getWritableDatabase();   //get instance of database, inherited from SQLiteOpenHelper
        String[] columns = {JournalConstants.DATE, JournalConstants.ENTRY};
        Cursor cursor = db.query(JournalConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
        return cursor;

    }


}