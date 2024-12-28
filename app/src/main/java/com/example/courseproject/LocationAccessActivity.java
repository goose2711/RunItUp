package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;


public class LocationAccessActivity extends AppCompatActivity implements View.OnClickListener {

    Button saveButton;
    ImageButton exitButton;
    boolean checked = false;

    String accent;    //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    TextView titleTextView;

    private boolean userInitialized; //for saving if account has already been created (do doesn't go though series of start screens again)

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_access);

        titleTextView = (TextView) findViewById(R.id.titleTextView);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings

        userInitialized = sp.getBoolean("userInitialized", false); //get if user is new or not (if has gone through start screens or not)

        if (userInitialized) {  //if editing later in settings
            exitButton.setVisibility(View.VISIBLE); //exit to settings button visible if accessing from settings
            saveButton.setText("SAVE");
                ActivityCompat.requestPermissions(LocationAccessActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

        } else {    //if new user
            exitButton.setVisibility(View.INVISIBLE); //exit button not visible if filling in data for first time
            checkLocationPermission();  //check location permissions
        }
    }




    @Override
    public void onClick(View v) {

        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:
                if (!userInitialized) {   //first time user is creating account so go to next data screen
                    gotoWeatherPreference();
                } else {
                    gotoSettings();
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to settings without saving anything
                gotoSettings();
                break;
        }
    }


    //sends new user to next user data screen
    private void gotoWeatherPreference(){
        Intent intent = new Intent(this, WeatherPreferenceActivity.class);
        startActivity(intent);
    }


    //sends existing user to settings
    private void gotoSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra("prevActivity", "location_access_activity"); //tell settings that just came from location access activity (to make it check if location access is on
        startActivity(intent);
    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                builder.setTitle("Enable location access");
                builder.setMessage("Please enable location acces to allow route tracking and local temperature information retrieval");

                // Set up the buttons
                builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LocationAccessActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                });


            } else {    //request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}