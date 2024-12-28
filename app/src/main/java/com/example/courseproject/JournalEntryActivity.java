package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

//activity for adding a journal entry
public class JournalEntryActivity extends AppCompatActivity implements View.OnClickListener {

    JournalDatabase jdb;

    Button saveButton;
    ImageButton exitButton;

    TextView titleTextView;
    EditText entryEditText;

    public static final String DEFAULT = "not available";
    public static final String DEFAULT_COLOR = "#FF4081";
    String accent;  //for colour theme

    String previousActivity;    //for going back to correct activity if exited

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_journal_entry);

        saveButton = (Button) findViewById(R.id.saveButton); //save button for saving journal entry
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //save button for saving journal entry
        exitButton.setOnClickListener(this);

        titleTextView = (TextView) findViewById(R.id.journalTitleTextView); //initialize title textview

        previousActivity = getIntent().getExtras().getString("prevActivity");   //get activity that started this activity

        entryEditText = (EditText) findViewById(R.id.entryContent);


        jdb = new JournalDatabase(this);    //initialize instance of database to access journal entries

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sharedPrefs);

    }

    //get curent date of entry
    private String getDate() {
        //get current date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        //variable for current date and time and calling a simple date format in it.
        String currentDate = sdf.format(new Date());

        return currentDate;
    }

    public void addEntry() {
        String entry = entryEditText.getText().toString();

        if (!entry.equals("")) { //only save the data if there is an entry added

            long id = jdb.insertData(getDate(), entry);   //add current date and entry

            if (id < 0) {
//                Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
            }

        } else {
//            Toast.makeText(this, "no entry added", Toast.LENGTH_SHORT).show();
        }

    }


    //react to button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:   //if save button is pressed save entry and return to main journal activity
                addEntry();
                gotoJournal();  //return to main journal page
                break;

            case R.id.exitButton:   //if exit button is pressed return to main journal activity
                switch (previousActivity) {
                    case "journal_activity":    //from run log add run page
                        gotoJournal();  //return to main journal page
                        break;

                    case "run_activity":    //from post run schedule button
                        gotoRun();  //if came from post run activity return to run log page
                        break;
                }
                break;
        }
    }

    //explicit intents for transitioning to other app screens from navigation
    private void gotoJournal() {
        Intent journalIntent = new Intent(this, JournalActivity.class);
        startActivity(journalIntent);
    }

    public void gotoRun() {
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text and button color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));
    }
}


