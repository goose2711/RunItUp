package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class FutureRunScheduleActivity extends AppCompatActivity implements View.OnClickListener {

    TextView titleTextView;

    public static final String DEFAULT = "not available";
    public static final String DEFAULT_COLOR = "#FF4081";
    String accent;  //for colour theme

    ImageButton exitButton, addRunButton;

    //for database access
    private RunDatabase rdb;
    private RunDatabaseHelper runDatabaseHelper;
    Cursor cursor;

    //for recyclerview scroll of preferences
    private RecyclerView runRecycler;
    private RecyclerView.Adapter runAdapter;

    private RecyclerView.LayoutManager runLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_future_run_schedule);

        titleTextView = (TextView) findViewById(R.id.titleTextView);

        exitButton = (ImageButton) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(this);

        addRunButton = (ImageButton) findViewById(R.id.addButton);
        addRunButton.setOnClickListener(this);

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        updateColorTheme(sharedPrefs);  //update color theme based on settings

        try {
            setupRecycler();    //get data from stats sqlite database and set up recyclerview with stats by run
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //sets up recyclerview layout and gets data from database
    private void setupRecycler() throws ParseException {

        rdb = new RunDatabase(this);
        runDatabaseHelper = new RunDatabaseHelper(this);    //creating database

        // get reference to RecyclerView from the layout
        runRecycler = (RecyclerView) findViewById(R.id.runScheduleRecyclerView);

        cursor = rdb.getData();

        //get data for recyclerview
        int dateIndex = cursor.getColumnIndex(RunConstants.DATE);
        int timeIndex = cursor.getColumnIndex(RunConstants.TIME);
        int kmIndex = cursor.getColumnIndex(RunConstants.DISTANCE);
        int messageIndex = cursor.getColumnIndex(RunConstants.MESSAGE);

        ArrayList<String> runScheduleArrayList = new ArrayList<String>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
            String date = cursor.getString(dateIndex);
            String time = cursor.getString(timeIndex);
            String km = cursor.getString(kmIndex);
            String message = cursor.getString(messageIndex);

            //convert time to 12 h with am/pm
            try {
                SimpleDateFormat time24h = new SimpleDateFormat("HH:mm");
                SimpleDateFormat time12h = new SimpleDateFormat("hh:mm a");
                Date nextRunTimeDate = time24h.parse(time);
                time = (time12h.format(nextRunTimeDate));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String runSchedule = date + "," + time + "," + km + "," + message;
            runScheduleArrayList.add(runSchedule);
            cursor.moveToNext();    //go to next row (if there is one)
        }

        //Sort entries from most to least recent (is this good to do?)
        String[] results;
        int mindex;
        Date minDate;

        for (int i = 0; i < runScheduleArrayList.size(); i++) {
            results = (runScheduleArrayList.get(i).toString()).split(","); //split results using ,
            minDate = new SimpleDateFormat("dd-MM-yyyy").parse(results[0]);
            mindex = i;

            for (int j = i + 1; j < runScheduleArrayList.size(); j++) {
                String[] dateRunArray = (runScheduleArrayList.get(j).toString()).split(","); //split results using ,
                Date dateToCompare = new SimpleDateFormat("dd-MM-yyyy").parse(dateRunArray[0]);   //get future run date

                if (dateToCompare.after(minDate)) {    //if date is after current date
                    minDate = dateToCompare;
                    mindex = j;
                }
            }
            Collections.swap(runScheduleArrayList, mindex, i);
        }

        //make arraylist in chronological order

        // use a linear layout manager
        runLayoutManager = new LinearLayoutManager(this);
        runRecycler.setLayoutManager(runLayoutManager);

        // initialize and set the adapter for the RecyclerView
        runAdapter = new FutureRunCustomAdapter(runScheduleArrayList);

        runRecycler.setAdapter(runAdapter);
    }

    private void gotoDashboard() {
        Intent i = new Intent(this, DashboardActivity.class);
        startActivity(i);
    }

    private void gotoFutureRunSchedule() {
        Intent intent = new Intent(this, RunSchedulingCalendarActivity.class);
        intent.putExtra("input_type", "future");
        intent.putExtra("prevActivity", "future_run_schedule_activity"); //what activity started the intent/activity
        startActivity(intent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.exitButton:   //if exit pressed return to dashboard
                gotoDashboard();
                break;

            case R.id.addButton:   //if add entry button pressed go to add entry activity
                gotoFutureRunSchedule();
                break;
        }
    }
}