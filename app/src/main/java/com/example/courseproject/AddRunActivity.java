package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AddRunActivity extends AppCompatActivity implements View.OnClickListener {

    Button newRunButton, pastRunButton, futureRunButton;
    ImageButton exitButton;
    TextView titleTextView;

    String accent, accentLight;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    public static final String DEFAULT_LIGHT = "#FF9F9F";

    public static final String DEFAULT = "not available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_run);

        titleTextView = (TextView) findViewById(R.id.addRunTitleTextView);

        newRunButton = (Button) findViewById(R.id.newRunButton);
        newRunButton.setOnClickListener(this);

        pastRunButton = (Button) findViewById(R.id.pastRunButton);
        pastRunButton.setOnClickListener(this);

        futureRunButton = (Button) findViewById(R.id.futureRunButton);
        futureRunButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(this);

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sharedPrefs);  //update color theme based on settings
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {

            case R.id.newRunButton:
                gotoCurrentRun();
                break;

            case R.id.pastRunButton:
                gotoPastRunInput();
                break;

            case R.id.futureRunButton:
                gotoFutureRunSchedule();
                break;

            case R.id.exitButton:
                gotoRunLog();
                break;
        }
    }

    private void gotoCurrentRun() {
        Intent intent = new Intent(this, CurrentRunActivity.class);
        startActivity(intent);
    }

    private void gotoFutureRunSchedule() {
        Intent intent = new Intent(this, RunSchedulingCalendarActivity.class);
        intent.putExtra("input_type", "future");
        intent.putExtra("prevActivity", "add_run_activity");
        startActivity(intent);
    }

    private void gotoPastRunInput() {
        Intent intent = new Intent(this, RunSchedulingCalendarActivity.class);
        intent.putExtra("input_type", "past");
        intent.putExtra("prevActivity", "add_run_activity");
        startActivity(intent);
    }

    private void gotoRunLog() {
        Intent addRunIntent = new Intent(this, RunActivity.class);
        startActivity(addRunIntent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        accentLight = sharedPrefs.getString("themeColorLight", DEFAULT_LIGHT);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        newRunButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
        pastRunButton.setBackgroundColor(Color.parseColor(accentLight));
        futureRunButton.setBackgroundColor(Color.parseColor(accentLight));
    }


}