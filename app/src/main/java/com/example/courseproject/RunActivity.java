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
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class RunActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {

    ImageButton addRunButton;
    TextView titleTextView;

    BottomNavigationView nav;

    String accent, accentLight;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    public static final String DEFAULT_LIGHT = "#FF9F9F";

    public static final String DEFAULT = "not available";

    //for database access
    private StatsDatabase sdb;
    private StatsDatabaseHelper statsDatabaseHelper;
    Cursor cursor;

    //for recyclerview scroll of preferences
    private RecyclerView statsRecycler;
    private RecyclerView.Adapter statsAdapter;

    private RecyclerView.LayoutManager statsLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(this);

        nav.getMenu().findItem(R.id.nav_run).setChecked(true);  //set correct icon checked

        addRunButton = (ImageButton) findViewById(R.id.addButton);
        addRunButton.setOnClickListener(this);

        titleTextView = (TextView) findViewById(R.id.addRunTitleTextView);  //for color change with theme

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sharedPrefs);  //update color theme based on settings

        //get username and password saved in sharedprefs
        String savedUsername = sharedPrefs.getString("username", DEFAULT);

        //if saved username has content
        if (!savedUsername.equals(DEFAULT)) {
            titleTextView.setText(savedUsername + "'s Run Log"); //personalized activity title
        }

        try {
            setupRecycler();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    //sets up recyclerview layout and gets data from database
    private void setupRecycler() throws ParseException {

        sdb = new StatsDatabase(this);
        statsDatabaseHelper = new StatsDatabaseHelper(this);    //creating database

        // get reference to RecyclerView from the layout
        statsRecycler = (RecyclerView) findViewById(R.id.runStatsRecycler);

        cursor = sdb.getRunDataStats();

        //get data for recyclerview
        int dateIndex = cursor.getColumnIndex(StatsConstants.DATE);
        int timeIndex = cursor.getColumnIndex(StatsConstants.TIME);
        int stepIndex = cursor.getColumnIndex(StatsConstants.STEPS);
        int kmIndex = cursor.getColumnIndex(StatsConstants.KM);
        int caloriesIndex = cursor.getColumnIndex(StatsConstants.CALORIES);
        int paceIndex = cursor.getColumnIndex(StatsConstants.PACE);
        int uidIndex = cursor.getColumnIndex(StatsConstants.UID);

        ArrayList<String> statsByRunArrayList = new ArrayList<String>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
            String date = cursor.getString(dateIndex);
            String time = cursor.getString(timeIndex);
            String steps = cursor.getString(stepIndex);
            String km = cursor.getString(kmIndex);
            String calories = cursor.getString(caloriesIndex);
            String pace = cursor.getString(paceIndex);
            String UID = cursor.getString(uidIndex);

            DecimalFormat df = new DecimalFormat("###.##");   //round to 2 decimal places
            double paceDouble = Double.parseDouble(pace);
            String paceFormatted = df.format(paceDouble);

            String stats = date + "," + time + "," + steps + "," + km + "," + calories + "," + paceFormatted +","+ UID;
            statsByRunArrayList.add(stats);
            cursor.moveToNext();    //go to next row (if there is one)
        }

        //Sort entries from most to least recent
        String[] results;
        int mindex;
        Date minDate;

        for (int i = 0; i < statsByRunArrayList.size(); i++) {
            results = (statsByRunArrayList.get(i).toString()).split(","); //split results using ,
            minDate = new SimpleDateFormat("dd-MM-yyyy").parse(results[0]);
            mindex = i;

            for (int j = i + 1; j < statsByRunArrayList.size(); j++) {
                String[] dateRunArray = (statsByRunArrayList.get(j).toString()).split(","); //split results using ,
                Date dateToCompare = new SimpleDateFormat("dd-MM-yyyy").parse(dateRunArray[0]);   //get future run date

                if (dateToCompare.after(minDate)) {    //if date is after current date
                    minDate = dateToCompare;
                    mindex = j;
                }
            }
            Collections.swap(statsByRunArrayList, mindex, i);
        }

        // use a linear layout manager
        statsLayoutManager = new LinearLayoutManager(this);
        statsRecycler.setLayoutManager(statsLayoutManager);

        // initialize and set the adapter for the RecyclerView
        statsAdapter = new StatsByRunCustomAdapter(statsByRunArrayList);

        statsRecycler.setAdapter(statsAdapter);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {

            case R.id.addButton:
                gotoAddRun();
                break;

        }
    }

    //go to add run activity
    private void gotoAddRun() {
        Intent intent = new Intent(this, AddRunActivity.class);
        startActivity(intent);
    }

    //explicit intents for transitioning to other app screens from navigation
    public void gotoDash() {
        Intent dashIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashIntent);
    }

    private void gotoStats() {
        Intent statsIntent = new Intent(this, StatsActivity.class);
        startActivity(statsIntent);
    }

    private void gotoJournal() {
        Intent journalIntent = new Intent(this, JournalActivity.class);
        startActivity(journalIntent);
    }

    private void gotoRecovery() {
        Intent recoveryIntent = new Intent(this, RecoveryActivity.class);
        startActivity(recoveryIntent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        accentLight = sharedPrefs.getString("themeColorLight", DEFAULT_LIGHT);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
    }

    //bottom navigation bar
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                gotoDash();
                break;

            case R.id.nav_stats:
                gotoStats();
                break;

            case R.id.nav_recovery:
                gotoRecovery();
                break;

            case R.id.nav_journal:
                gotoJournal();
                break;

        }
        return true;
    }

}