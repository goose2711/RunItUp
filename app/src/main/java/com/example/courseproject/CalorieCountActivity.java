package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CalorieCountActivity extends AppCompatActivity implements View.OnClickListener {

    Button saveButton;
    ImageButton exitButton;
    ToggleButton calorieToggle;
    boolean checked = false;

    String accent;    //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    TextView titleTextView;

    private boolean userInitialized; //for saving if account has already been created (do doesn't go though series of start screens again)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_count);

        titleTextView = (TextView) findViewById(R.id.calorieCountTextView);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        calorieToggle = (ToggleButton) findViewById(R.id.calorieToggle);
        calorieToggle.setOnCheckedChangeListener(checkChangeListener);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings

        userInitialized = sp.getBoolean("userInitialized", false); //get if user is new or not (if has gone through start screens or not)

        if (userInitialized) {
            exitButton.setVisibility(View.VISIBLE); //exit to settings button visible if accessing from settings

            if (sp.getBoolean("calorieCount", checked)) {    //if calorie count set to on, set on
                calorieToggle.setChecked(true);
            }
        } else {
            exitButton.setVisibility(View.INVISIBLE); //exit button not visible if filling in data for first time
        }
    }

    //inner class for togglebutton check change listener
    private CompoundButton.OnCheckedChangeListener checkChangeListener = new CompoundButton.OnCheckedChangeListener() {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                checked = true;
            } else {
                checked = false;
            }
        }
    };

    public void saveUserData() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPrefs.edit();

        //putting data in with key value pairs
        editor.putBoolean("calorieCount", checked);

        editor.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:
                if (!userInitialized) {   //first time user is creating account so go to next data screen
                    saveUserData();
                    gotoRunScheduling();
                } else {
                    saveUserData(); //user is changing settings retroactively so go back to settings
                    gotoSettings();
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to settings without saving anything
                gotoSettings();
                break;
        }
    }

    //sends new user to next user data screen
    private void gotoRunScheduling() {
        Intent intent = new Intent(this, RunSchedulingActivity.class);
        startActivity(intent);
    }

    //sends existing user to settings
    private void gotoSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }

}