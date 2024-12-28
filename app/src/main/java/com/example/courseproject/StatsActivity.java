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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {

    TextView titleTextView;

    BottomNavigationView nav;

    public static final String DEFAULT = "not available";

    public static final String DEFAULT_COLOR = "#FF4081";
    String accent;  //for colour theme

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
        setContentView(R.layout.activity_stats);

        nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(this);
        nav.getMenu().findItem(R.id.nav_stats).setChecked(true);  //set correct icon checked

        titleTextView = (TextView) findViewById(R.id.statsTextView);

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //get username and password saved in sharedprefs
        String savedUsername = sharedPrefs.getString("username", DEFAULT);

        //if saved username has content
        if (!savedUsername.equals(DEFAULT)) {
            titleTextView.setText(savedUsername + "'s Stats"); //personalized activity title
        }

        updateColorTheme(sharedPrefs);  //update color theme based on settings

        try {
            setupRecycler();    //get data from stats sqlite database and set up recyclerview with stats by run
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //sets up recyclerview layout and gets data from database
    private void setupRecycler() throws ParseException {

        sdb = new StatsDatabase(this);
        statsDatabaseHelper = new StatsDatabaseHelper(this);    //creating database

        // get reference to RecyclerView from the layout
        statsRecycler = (RecyclerView) findViewById(R.id.statsRecyclerView);

        cursor = sdb.getRunDataStats();

        //get data for recyclerview
        int dateIndex = cursor.getColumnIndex(StatsConstants.DATE);
        int timeIndex = cursor.getColumnIndex(StatsConstants.TIME);
        int stepIndex = cursor.getColumnIndex(StatsConstants.STEPS);
        int kmIndex = cursor.getColumnIndex(StatsConstants.KM);
        int caloriesIndex = cursor.getColumnIndex(StatsConstants.CALORIES);
        int paceIndex = cursor.getColumnIndex(StatsConstants.PACE);

        ArrayList<String> statsByRunArrayList = new ArrayList<String>();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
            String date = cursor.getString(dateIndex);
            String time = cursor.getString(timeIndex);
            String steps = cursor.getString(stepIndex);
            String km = cursor.getString(kmIndex);
            String calories = cursor.getString(caloriesIndex);
            String pace = cursor.getString(paceIndex);

            String stats = date + "," + time + "," + steps + "," + km + "," + calories + "," + pace;
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


        //stat calculations for display in recycler

        //get dates for stats
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusWeeks(1);
        LocalDate monthAgo = today.minusMonths(1);
        LocalDate sixMonthsAgo = today.minusMonths(6);
        LocalDate yearAgo = today.minusYears(1);

        ArrayList<String> dataByStatArrayList = new ArrayList<String>();

        String[] statResultString;
        LocalDate resultDate;

        int activityTimeWeek = 0, activityTimeMonth = 0, activityTimeSixMonth = 0, activityTimeYear = 0, activityTimeLifetime = 0;
        double kmWeek = 0, kmMonth = 0, kmSixMonth = 0, kmYear = 0, kmLifetime = 0;
        int stepsWeek = 0, stepsMonth = 0, stepsSixMonth = 0, stepsYear = 0, stepsLifetime = 0;
        double caloriesWeek = 0, caloriesMonth = 0, caloriesSixMonth = 0, caloriesYear = 0, caloriesLifetime = 0;
        double paceWeek, paceMonth, paceSixMonth, paceYear, paceLifetime;

        for (int m = 0; m < statsByRunArrayList.size(); m++) { //loop through all, fill each with stat data
            statResultString = (statsByRunArrayList.get(m).toString()).split(","); //split results using ,
            resultDate = LocalDate.parse(statResultString[0], DateTimeFormatter.ofPattern("dd-MM-yyyy"));   //get localdate of specific run

            int activityTimeTemp = Integer.parseInt(statResultString[1]);   //get activity time from specific run
            int stepsTemp = Integer.parseInt(statResultString[2]);
            double kmTemp = Double.parseDouble(statResultString[3]);
            double caloriesTemp = Double.parseDouble(statResultString[4]);

            if (resultDate.isAfter(weekAgo)) {   //if result is in last week add to totals
                activityTimeWeek += activityTimeTemp;
                kmWeek += kmTemp;
                stepsWeek += stepsTemp;
                caloriesWeek += caloriesTemp;
            }
            if (resultDate.isAfter(monthAgo)) {   //if result is in last month add to totals
                activityTimeMonth += activityTimeTemp;
                kmMonth += kmTemp;
                stepsMonth += stepsTemp;
                caloriesMonth += caloriesTemp;
            }
            if (resultDate.isAfter(sixMonthsAgo)) {   //if result is in last 6 months add to totals
                activityTimeSixMonth += activityTimeTemp;
                kmSixMonth += kmTemp;
                stepsSixMonth += stepsTemp;
                caloriesSixMonth += caloriesTemp;
            }
            if (resultDate.isAfter(yearAgo)) {   //if result is in last year add to totals
                activityTimeYear += activityTimeTemp;
                kmYear += kmTemp;
                stepsYear += stepsTemp;
                caloriesYear += caloriesTemp;
            }

            //lifetime stats (add up all)
            activityTimeLifetime += activityTimeTemp;
            kmLifetime += kmTemp;
            stepsLifetime += stepsTemp;
            caloriesLifetime += caloriesTemp;
        }

        //calculate pace averages (Catch if dividing by 0)
        if (activityTimeWeek != 0 && kmWeek != 0) {    //pace of week
            double mins = activityTimeWeek / 60;
            double hrs = mins / 60;
            double paceDouble = kmWeek / hrs;
            paceWeek = paceDouble;
        } else {
            paceWeek = 0;
        }

        if (activityTimeMonth != 0 && kmMonth != 0) {   //pace of month
            double mins = activityTimeMonth / 60;
            double hrs = mins / 60;
            double paceDouble = kmMonth / hrs;
            paceMonth = paceDouble;
        } else {
            paceMonth = 0;
        }
        if (activityTimeSixMonth != 0 && kmSixMonth != 0) {    //pace of last 6 months
            double mins = activityTimeSixMonth / 60;
            double hrs = mins / 60;
            double paceDouble = kmSixMonth / hrs;
            paceSixMonth = paceDouble;
        } else {
            paceSixMonth = 0;
        }
        if (activityTimeYear != 0 && kmYear != 0) {    //pace of last year
            double mins = activityTimeYear / 60;
            double hrs = mins / 60;
            double paceDouble = kmYear / hrs;
            paceYear = paceDouble;
        } else {
            paceYear = 0;
        }
        if (activityTimeLifetime != 0 && kmLifetime != 0) {    //lifetime average pace
            double mins = activityTimeLifetime / 60;
            double hrs = mins / 60;
            double paceDouble = kmLifetime / hrs;
            paceLifetime = paceDouble;
        } else {
            paceLifetime = 0;
        }

        if (paceWeek == Double.POSITIVE_INFINITY){
            paceWeek = 0;
        }
        if (paceMonth == Double.POSITIVE_INFINITY){
            paceMonth = 0;
        }
        if (paceSixMonth == Double.POSITIVE_INFINITY){
            paceSixMonth = 0;
        }
        if (paceYear == Double.POSITIVE_INFINITY){
            paceYear = 0;
        }
        if (paceLifetime == Double.POSITIVE_INFINITY){
            paceLifetime = 0;
        }

        DecimalFormat df = new DecimalFormat("###.##");   //round to 2 decimal places

        String activityTimeStatsString = "ACTIVITY TIME" + "," + formatTime(activityTimeWeek) + "," + formatTime(activityTimeMonth) + "," + formatTime(activityTimeSixMonth) + "," + formatTime(activityTimeYear) + "," + formatTime(activityTimeLifetime);
        String kmStatsString = "KM TRAVELED" + "," + Double.parseDouble(df.format(kmWeek)) + "," + Double.parseDouble(df.format(kmMonth)) + "," + Double.parseDouble(df.format(kmSixMonth)) + "," + Double.parseDouble(df.format(kmYear)) + "," + Double.parseDouble(df.format(kmLifetime));
        String stepsStatsString = "STEPS" + "," + stepsWeek + "," + stepsMonth + "," + stepsSixMonth + "," + stepsYear + "," + stepsLifetime;
        String paceStatsString = "AVERAGE PACE" + "," + Double.parseDouble(df.format(paceWeek)) + "," + Double.parseDouble(df.format(paceMonth)) + "," + Double.parseDouble(df.format(paceSixMonth)) + "," + Double.parseDouble(df.format(paceYear)) + "," + Double.parseDouble(df.format(paceLifetime));
        String caloriesStatsString = "CALORIES BURNED" + "," + Double.parseDouble(df.format(caloriesWeek)) + "," + Double.parseDouble(df.format(caloriesMonth)) + "," + Double.parseDouble(df.format(caloriesSixMonth)) + "," + Double.parseDouble(df.format(caloriesYear)) + "," + Double.parseDouble(df.format(caloriesLifetime));

        dataByStatArrayList.add(activityTimeStatsString);
        dataByStatArrayList.add(kmStatsString);
        dataByStatArrayList.add(stepsStatsString);
        dataByStatArrayList.add(paceStatsString);
        dataByStatArrayList.add(caloriesStatsString);


        // use a linear layout manager
        statsLayoutManager = new LinearLayoutManager(this);
        statsRecycler.setLayoutManager(statsLayoutManager);

        // initialize and set the adapter for the RecyclerView
        statsAdapter = new StatsByStatCustomAdapter(dataByStatArrayList);

        statsRecycler.setAdapter(statsAdapter);

    }

    //format seconds into hh mm ss
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {

        }
    }

    //explicit intents for transitioning to other app screens from navigation
    public void gotoDash() {
        Intent dashIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashIntent);
    }

    private void gotoRecovery() {
        Intent recoveryIntent = new Intent(this, RecoveryActivity.class);
        startActivity(recoveryIntent);
    }

    private void gotoJournal() {
        Intent journalIntent = new Intent(this, JournalActivity.class);
        startActivity(journalIntent);
    }

    private void gotoAddRun() {
        Intent addRunIntent = new Intent(this, RunActivity.class);
        startActivity(addRunIntent);
    }



    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
    }

    //bottom navigation bar
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                gotoDash();
                break;

            case R.id.nav_run:
                gotoAddRun();
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