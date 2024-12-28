package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class RecoveryActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    TextView titleTextView;

    BottomNavigationView nav;

    public static final String DEFAULT = "not available";

    public static final String DEFAULT_COLOR = "#FF4081";
    String accent;  //for colour change with theme

    //for recyclerview scroll
    private RecyclerView recoveryRecycler;
    private RecyclerView.Adapter recoveryAdapter;
    private RecyclerData recoveryContent;
    private RecyclerView.LayoutManager recoveryLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(this);
        nav.getMenu().findItem(R.id.nav_recovery).setChecked(true);  //set correct icon checked

        titleTextView = (TextView) findViewById(R.id.recoveryTitleTextView);


        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //get username and password saved in sharedprefs
        String savedUsername = sharedPrefs.getString("username", DEFAULT);

        //if saved username has content
        if (!savedUsername.equals(DEFAULT)) {
            titleTextView.setText(savedUsername + "'s Recovery"); //personalized activity title
        }

        setupRecycler();    //set up recyclerview layout

        updateColorTheme(sharedPrefs);  //update color theme based on settings
    }

    //sets up recyclerview layout
    private void setupRecycler() {
        // get reference to RecyclerView from the layout
        recoveryRecycler = (RecyclerView) findViewById(R.id.recoveryRecyclerView);
        // initialize the data
        recoveryContent = new RecyclerData();

        // use a linear layout manager
        recoveryLayoutManager = new LinearLayoutManager(this);
        recoveryRecycler.setLayoutManager(recoveryLayoutManager);

        // initialize and set the adapter for the RecyclerView
        recoveryAdapter = new RecoveryCustomAdapter(recoveryContent);
        recoveryRecycler.setAdapter(recoveryAdapter);

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

    private void gotoAddRun() {
        Intent addRunIntent = new Intent(this, RunActivity.class);
        startActivity(addRunIntent);
    }

    private void gotoSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
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

            case R.id.nav_stats:
                gotoStats();
                break;

            case R.id.nav_run:
                gotoAddRun();
                break;

            case R.id.nav_journal:
                gotoJournal();
                break;

        }
        return true;
    }


}