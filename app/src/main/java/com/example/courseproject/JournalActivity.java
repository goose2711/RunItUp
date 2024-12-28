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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class JournalActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {

    ImageButton addEntryButton;
    TextView titleTextView;

    BottomNavigationView nav;

    public static final String DEFAULT = "not available";
    public static final String DEFAULT_COLOR = "#FF4081";
    String accent;  //for colour theme

    //for database access
    private JournalDatabase jdb;
    private JournalDatabaseHelper journalDatabaseHelper;
    Cursor cursor;

    //for recyclerview scroll of preferences
    private RecyclerView journalRecycler;
    private RecyclerView.Adapter journalAdapter;

    private RecyclerView.LayoutManager journalLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(this);
        nav.getMenu().findItem(R.id.nav_journal).setChecked(true);  //set correct icon checked

        titleTextView = (TextView) findViewById(R.id.journalTitleTextView);

        addEntryButton = (ImageButton) findViewById(R.id.addButton);
        addEntryButton.setOnClickListener(this);


        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //get username and password saved in sharedprefs
        String savedUsername = sharedPrefs.getString("username", DEFAULT);

        //if saved username has content
        if (!savedUsername.equals(DEFAULT)) {
            titleTextView.setText(savedUsername + "'s Journal"); //personalized activity title
        }

        updateColorTheme(sharedPrefs);  //update color theme based on settings

        try {
            setupRecycler();    //set up recyclerview and access database (to fill with journal entry content)
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    //sets up recyclerview layout and gets data from database
    private void setupRecycler() throws ParseException {

        jdb = new JournalDatabase(this);
        journalDatabaseHelper = new JournalDatabaseHelper(this);    //creating database
        journalRecycler = (RecyclerView) findViewById(R.id.journalRecyclerView);    // get reference to RecyclerView from the layout
        cursor = jdb.getData();

        int index1 = cursor.getColumnIndex(JournalConstants.DATE); //get data for recyclerview
        int index2 = cursor.getColumnIndex(JournalConstants.ENTRY);

        ArrayList<String> journalEntriesArrayList = new ArrayList<String>();
        cursor.moveToFirst();

        //get list of journal entries/dates
        while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
            String entryDate = cursor.getString(index1);
            String entryContent = cursor.getString(index2);
            String s = entryDate + "," + entryContent;
            journalEntriesArrayList.add(s);
            cursor.moveToNext();    //go to next row (if there is one)
        }

        //Sort entries from most to least recent
        String[] results;
        int mindex;
        Date minDate;

        for (int i = 0; i < journalEntriesArrayList.size(); i++) {
            results = (journalEntriesArrayList.get(i).toString()).split(","); //split results using ,
            minDate = new SimpleDateFormat("dd-MM-yyyy").parse(results[0]);
            mindex = i;

            for (int j = i + 1; j < journalEntriesArrayList.size(); j++) {
                String[] dateEntryArray = (journalEntriesArrayList.get(j).toString()).split(","); //split results using ,
                Date dateToCompare = new SimpleDateFormat("dd-MM-yyyy").parse(dateEntryArray[0]);   //get future run date

                if (dateToCompare.after(minDate)) {    //if date is after current date
                    minDate = dateToCompare;
                    mindex = j;
                }
            }
            Collections.swap(journalEntriesArrayList, mindex, i);
        }
        //put entries in recyclerview
        journalLayoutManager = new LinearLayoutManager(this);   // use a linear layout manager
        journalRecycler.setLayoutManager(journalLayoutManager);
        journalAdapter = new JournalCustomAdapter(journalEntriesArrayList); // initialize and set the adapter for the RecyclerView
        journalRecycler.setAdapter(journalAdapter);
    }


    //react to button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.addButton:   //if add entry button pressed go to add entry activity
                gotoAddEntry();
                break;
        }
    }

    private void gotoAddEntry() {    //go to add journal entry activity
        Intent addEntryIntent = new Intent(this, JournalEntryActivity.class);
        addEntryIntent.putExtra("prevActivity", "journal_activity"); //what activity started the intent/activity (what to go back to if user presses x)
        startActivity(addEntryIntent);
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

    private void gotoRecovery() {
        Intent recoveryIntent = new Intent(this, RecoveryActivity.class);
        startActivity(recoveryIntent);
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

            case R.id.nav_recovery:
                gotoRecovery();
                break;

        }
        return true;
    }
}