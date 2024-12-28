package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherPreferenceActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    Button saveButton;
    ImageButton exitButton;
    String weatherPreference;

    private RadioGroup weatherPreferenceRadioGroup;
    private RadioButton hotButton, mildButton, coldButton, noPrefButton;

    private boolean userInitialized; //for saving if account has already been created (do doesn't go though series of start screens again)

    //temperature preference constants
    public static final String HOT = "Hot";
    public static final String MILD = "Mild";
    public static final String COLD = "Cold";
    public static final String NO_PREF = "No Preference";

    public static final String DEFAULT_COLOR = "#FF4081";  //for colour theme
    private String accent;
    TextView titleTextView;

    private boolean locationToggle = false; //for saving if location was enabled in previous screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_preference);

        //initialize radio group for bg color
        weatherPreferenceRadioGroup = (RadioGroup) findViewById(R.id.weatherPreferenceRadioGroup);
        weatherPreferenceRadioGroup.setOnCheckedChangeListener(this);

        titleTextView = (TextView) findViewById(R.id.weatherPreferenceTitleTextView);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);


        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings
        userInitialized = sp.getBoolean("userInitialized", false); //get if user is new or not (if has gone through start screens or not)

        if (userInitialized) {
            exitButton.setVisibility(View.VISIBLE); //exit to settings button visible if accessing from settings
            weatherPreference = sp.getString("weatherPreference", NO_PREF);
            switch (weatherPreference) {   //set current temperature selection checked if returning to edit in settings

                case HOT:
                    hotButton = (RadioButton) findViewById(R.id.hotButton);
                    hotButton.setChecked(true);
                    break;

                case MILD:
                    mildButton = (RadioButton) findViewById(R.id.mildButton);
                    mildButton.setChecked(true);
                    break;

                case COLD:
                    coldButton = (RadioButton) findViewById(R.id.coldButton);
                    coldButton.setChecked(true);
                    break;

                case NO_PREF:
                    noPrefButton = (RadioButton) findViewById(R.id.noPrefButton);
                    noPrefButton.setChecked(true);
                    break;
            }
        } else {
            exitButton.setVisibility(View.INVISIBLE); //exit button not visible if filling in data for first time
            checkLocationAccess();
        }
    }

    //update weather preference based on what is checked
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.hotButton:
                weatherPreference = HOT;
                break;

            case R.id.mildButton:
                weatherPreference = MILD;
                break;

            case R.id.coldButton:
                weatherPreference = COLD;
                break;

            case R.id.noPrefButton:
                weatherPreference = NO_PREF;
                break;
        }
    }


    //check which checkbox is clicked and save data accordingly
    public void onClick(View v) { //change theme variable and text/button color based on radio selection (gives preview)
        switch (v.getId()) {

            case R.id.saveButton:
                if (!userInitialized) {   //first time user is creating account so go to next data screen
                        saveUserData();
                        gotoMusicSync();
                } else {   //editing from settings
                        saveUserData();
                        gotoSettings();
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to settings without saving anything
                gotoSettings();
                break;

        }
    }


    public void saveUserData() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPrefs.edit();

        //putting data in with key value pairs
        editor.putString("weatherPreference", weatherPreference);

        if (!userInitialized){  //save previous screen location access toggle if new user
            editor.putBoolean("locationAccess", locationToggle);
        }

        editor.commit();
    }


    //sends new user to next user data screen
    private void gotoMusicSync() {
        Intent intent = new Intent(this, MusicSyncActivity.class);
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

    //check if location was enabled in last screen and update variable to save in sharedprefs
    private void checkLocationAccess(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationToggle = true;
        }else{
            locationToggle = false;
        }

    }


}