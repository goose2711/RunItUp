package com.example.courseproject;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class RunDatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    //string statement to create table
    private static final String CREATE_TABLE =
            "CREATE TABLE " +
                    RunConstants.TABLE_NAME + " (" +
                    RunConstants.UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RunConstants.DATE + " TEXT, " +
                    RunConstants.TIME + " TEXT, " +
                    RunConstants.DISTANCE + " TEXT, " +
                    RunConstants.MESSAGE + " TEXT);";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + RunConstants.TABLE_NAME;

    public RunDatabaseHelper(Context context) {
        super(context, RunConstants.DATABASE_NAME, null, RunConstants.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(CREATE_TABLE);
//            Toast.makeText(context, "onCreate() called", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
//            Toast.makeText(context, "exception onCreate() db", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {   //destroys old table and creates a new one
            db.execSQL(DROP_TABLE);
            onCreate(db);
//            Toast.makeText(context, "onUpgrade called", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
//            Toast.makeText(context, "exception onUpgrade() db", Toast.LENGTH_LONG).show();
        }
    }
}