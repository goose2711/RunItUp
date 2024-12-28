package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class PostRunActivity extends AppCompatActivity implements View.OnClickListener {

    String accent, accentLight;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    public static final String DEFAULT_LIGHT = "#FF9F9F";

    Button saveButton, addJournalEntryButton, scheduleNextRunButton;
    TextView titleTextView, timeTextView, stepsTextView, kmTextView, caloriesTextView, caloriesLabel, paceTextView;

    boolean calorieToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_run);

        //initialize navigation bar buttons
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        addJournalEntryButton = (Button) findViewById(R.id.addJournalEntryButton);
        addJournalEntryButton.setOnClickListener(this);

        scheduleNextRunButton = (Button) findViewById(R.id.scheduleNextRunButton);
        scheduleNextRunButton.setOnClickListener(this);

        titleTextView = (TextView) findViewById(R.id.postRunTitleTextView);
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
        kmTextView = (TextView) findViewById(R.id.kmTextView);
        caloriesTextView = (TextView) findViewById(R.id.caloriesTextView);
        caloriesLabel = (TextView) findViewById(R.id.caloriesLabel);
        paceTextView = (TextView) findViewById(R.id.paceTextView);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings

        calorieToggle = sp.getBoolean("calorieCount", false);      //get if calorie calculator is turned on or off

        updateStats();  //update run stats
    }

    //update run stats overview card with data from current run activity bundle
    private void updateStats() {
        String time = getIntent().getExtras().getString("time");
        int steps = getIntent().getExtras().getInt("steps");
        double km = getIntent().getExtras().getDouble("km");
        double calories = getIntent().getExtras().getDouble("calories");
        double pace = getIntent().getExtras().getDouble("pace");

        if (pace < 1) { //if not enough pace data to calculate set to uninitialized
            paceTextView.setText("-");
        } else {   //else show pace data if enough to calculate
            paceTextView.setText(Double.toString(pace));
        }

        timeTextView.setText(time); //set textviews to show updated run stats
        stepsTextView.setText(Integer.toString(steps));
        kmTextView.setText(Double.toString(km));
        caloriesTextView.setText(Double.toString(calories));

        if (!calorieToggle) {    //if calories turned off do not display calories burned stat
            caloriesTextView.setVisibility(View.GONE);
            caloriesLabel.setVisibility(View.GONE);
        } else {
            caloriesTextView.setVisibility(View.VISIBLE);
            caloriesLabel.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.addJournalEntryButton:    //go to add journal entry activity
                gotoAddEntry();
                break;

            case R.id.scheduleNextRunButton: //go to future run scheduling activity
                gotoFutureRunSchedule();
                break;

            case R.id.saveButton: //save run and go back to dashboard
                gotoRun();
                break;
        }
    }

    private void gotoAddEntry() {    //go to add journal entry activity
        Intent addEntryIntent = new Intent(this, JournalEntryActivity.class);
        addEntryIntent.putExtra("prevActivity", "run_activity"); //what activity started the intent/activity (what to go back to if user presses x)
        startActivity(addEntryIntent);
    }

    private void gotoFutureRunSchedule() {
        Intent intent = new Intent(this, RunSchedulingCalendarActivity.class);
        intent.putExtra("input_type", "future");
        intent.putExtra("prevActivity", "run_activity"); //what activity started the intent/activity (what to go back to if user presses x)
        startActivity(intent);
    }

    //explicit intents for transitioning to other app screens from navigation
    public void gotoDash() {
        Intent dashIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashIntent);
    }

    //explicit intents for transitioning to other app screens from navigation
    public void gotoRun() {
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        accentLight = sharedPrefs.getString("themeColorLight", DEFAULT_LIGHT);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        caloriesTextView.setTextColor(Color.parseColor(accent));
        stepsTextView.setTextColor(Color.parseColor(accent));
        kmTextView.setTextColor(Color.parseColor(accent));
        timeTextView.setTextColor(Color.parseColor(accent));
        paceTextView.setTextColor(Color.parseColor(accent));

        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
        addJournalEntryButton.setBackgroundColor(Color.parseColor(accentLight));
        scheduleNextRunButton.setBackgroundColor(Color.parseColor(accentLight));
    }

}