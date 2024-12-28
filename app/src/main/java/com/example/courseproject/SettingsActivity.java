package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton dashButton;
    TextView titleTextView;
    String accent;  //for updating colour theme
    public static final String DEFAULT_COLOR = "#FF4081";

    //for recyclerview scroll of preferences
    private RecyclerView settingsRecycler;
    private RecyclerView.Adapter settingsAdapter;
    private RecyclerData settingOptions;
    private RecyclerView.LayoutManager settingsLayoutManager;

    public static final String DEFAULT = "not available";

    private boolean locationToggle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //initialize navigation bar buttons
        dashButton = (ImageButton) findViewById(R.id.exitButton);
        dashButton.setOnClickListener(this);

        titleTextView = (TextView) findViewById(R.id.settingsTitleTextView);

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //get username and password saved in sharedprefs
        String savedUsername = sharedPrefs.getString("username", DEFAULT);

        //if saved username has content
        if (!savedUsername.equals(DEFAULT)) {
            titleTextView.setText(savedUsername + "'s Settings"); //personalized activity title
        }

        setupRecycler();    //set up recyclerview layout

        updateColorTheme(sharedPrefs);  //update color theme based on settings

        try {
            if (getIntent().getExtras().getString("prevActivity").equals("location_access_activity")) {  //if just came from location access activity page check permissions and update location toggle value in sharedprefs
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationToggle = true;
                }
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("locationAccess", locationToggle);
                editor.commit();
            }
        }catch (Exception e){
        }
    }


    //sets up recyclerview layout
    private void setupRecycler() {
        // get reference to RecyclerView from the layout
        settingsRecycler = (RecyclerView) findViewById(R.id.settingsRecyclerView);
        // initialize the data
        settingOptions = new RecyclerData();

        // use a linear layout manager
        settingsLayoutManager = new LinearLayoutManager(this);
        settingsRecycler.setLayoutManager(settingsLayoutManager);

        // initialize and set the adapter for the RecyclerView
        settingsAdapter = new SettingsCustomAdapter(settingOptions);
        settingsRecycler.setAdapter(settingsAdapter);

    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.exitButton:
                gotoDash();
                break;

        }
    }


    //explicit intents for transitioning to other app screens from navigation
    public void gotoDash() {
        Intent dashIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashIntent);
    }

}