package com.example.courseproject;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

//for saving running stats
public class StatsDatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    public int counter = 0;

    //string statement to create table
    private static final String CREATE_TABLE =
            "CREATE TABLE "+
                    StatsConstants.TABLE_NAME + " (" +
                    StatsConstants.UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    StatsConstants.DATE + " TEXT, " +
                    StatsConstants.KM + " TEXT, " +
                    StatsConstants.STEPS + " TEXT, " +
                    StatsConstants.TIME + " TEXT, " +
                    StatsConstants.CALORIES + " TEXT, " +
                    StatsConstants.PACE + " TEXT);" ;

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + StatsConstants.TABLE_NAME;

    public StatsDatabaseHelper(Context context){
        super (context, StatsConstants.DATABASE_NAME, null, StatsConstants.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(CREATE_TABLE);
            counter = counter + 1;  //increment counter so run route table name matches stats database
//            Toast.makeText(context, "onCreate() called", Toast.LENGTH_LONG).show();
        }
        catch (SQLException e) {
//            Toast.makeText(context, "exception onCreate() db", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {   //destroys old table and creates a new one

            //delete all run route tables
            String[] columns = {StatsConstants.UID};
            Cursor cursor = db.query(StatsConstants.TABLE_NAME, columns, null, null, null, null, null);  //return data from specified columns
            while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and delete map table associated with

                String DROP_MAP_TABLE = "DROP TABLE IF EXISTS " + cursor.getString(0);
                db.execSQL(DROP_MAP_TABLE);
                cursor.moveToNext();    //go to next row (if there is one)
            }

            db.execSQL(DROP_TABLE);
            onCreate(db);
//            Toast.makeText(context, "onUpgrade called", Toast.LENGTH_LONG).show();
        }
        catch (SQLException e) {
//            Toast.makeText(context, "exception onUpgrade() db", Toast.LENGTH_LONG).show();
        }
    }

    //create new table to save run route LatLngs
    public void addMapTable(SQLiteDatabase db, String tableID) {
        //string to create run route table
        String CREATE_MAP_TABLE =
                "CREATE TABLE "+
                        "MapData" + tableID + " (" +
                        StatsConstants.UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        StatsConstants.LAT + " TEXT, " +
                        StatsConstants.LNG +  " TEXT);" ;

        try {
            db.execSQL(CREATE_MAP_TABLE);
//            Toast.makeText(context, "onCreate() for map table called", Toast.LENGTH_LONG).show();
        }
        catch (SQLException e) {
//            Toast.makeText(context, "exception onCreate() db for map table"+e.toString(), Toast.LENGTH_LONG).show();
        }
    }


}