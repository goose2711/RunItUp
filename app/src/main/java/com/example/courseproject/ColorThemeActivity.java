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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

//settings activity for changing colour theme
public class ColorThemeActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private RadioGroup themeColorButton;
    private RadioButton pinkButton, orangeButton, greenButton, blueButton;

    String accent, accentLight;

    private Button saveButton;
    ImageButton exitButton;

    TextView titleTextView;

    //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";

    private boolean userInitialized = false; //for saving if account has already been created (do doesn't go though series of start screens again)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_theme);

        //initialize radio group for bg color
        themeColorButton = (RadioGroup) findViewById(R.id.themeColorRadioGroup);
        themeColorButton.setOnCheckedChangeListener(this);

        //initialize and set onclick listener for save button
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        titleTextView = (TextView) findViewById(R.id.colorThemeTextView);   //initialize textview (so can change color later

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings

        userInitialized = sp.getBoolean("userInitialized", false); //get if user is new or not (if has gone through start screens or not)

        if (userInitialized) {
            exitButton.setVisibility(View.VISIBLE); //exit to settings button visible if accessing from settings
            switch (accent) {   //set colour theme current selection checked

                case "#FF4081":  //if pink theme
                    pinkButton = (RadioButton) findViewById(R.id.pinkButton);
                    pinkButton.setChecked(true);
                    break;

                case "#FF9100":  //if orange theme
                    orangeButton = (RadioButton) findViewById(R.id.orangeButton);
                    orangeButton.setChecked(true);
                    break;

                case "#00D76E":  //if green theme
                    greenButton = (RadioButton) findViewById(R.id.greenButton);
                    greenButton.setChecked(true);
                    break;

                case "#536DFE":  //if blue theme
                    blueButton = (RadioButton) findViewById(R.id.blueButton);
                    blueButton.setChecked(true);
                    break;
            }
        } else {
            exitButton.setVisibility(View.INVISIBLE); //exit button not visible if filling in data for first time
        }
    }

    public void saveSettings() {
        //gives us an object with reference to sharedpreferences of app
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //create an editor object to use to put strings in sharedpreferences object
        SharedPreferences.Editor editor = sharedPrefs.edit();

        editor.putString("themeColor", accent);   //passing main theme colour into sharedpreferences
        editor.putString("themeColorLight", accentLight);   //passing accent colour into sharedpreferences

        editor.putBoolean("userInitialized", userInitialized);  //put in true if new user to let us know they have gone through all start screens

        editor.commit();    //commit the changes to sharedpreferences
    }


    //for radio group controlling colour theme preference
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) { //change theme variable and text/button color based on radio selection (gives preview)
        switch (checkedId) {
            case R.id.pinkButton:  //pink theme
                accent = "#FF4081";
                accentLight = "#FF9F9F";
                break;

            case R.id.orangeButton: //orange theme
                accent = "#FF9100";
                accentLight = "#E9B500";
                break;

            case R.id.greenButton: //green theme
                accent = "#00D76E";
                accentLight = "#A5CF4A";
                break;

            case R.id.blueButton:  //blue theme
                accent = "#536DFE";
                accentLight = "#4FC7ED";
                break;
        }
        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }


    @Override
    public void onClick(View v) {   //when click save button go back to main settings

        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:
                if (!userInitialized) {  //if seeing for the first time
                    userInitialized = true;
                    saveSettings(); //save settings to sharedpreferences
                    gotoDash(); //go back to main settings
                } else {   //if already established user editing settings retroactively
                    saveSettings(); //save settings to sharedpreferences
                    gotoSettings(); //go back to main settings
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to settings without saving anything
                gotoSettings();
                break;
        }
    }

    //sends new user to dashboard as they are done user data entry screens
    private void gotoDash() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }

    private void gotoSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR); //get color theme preference

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }


}