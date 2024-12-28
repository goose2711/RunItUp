package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UserDataInputActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    Button saveButton;
    ImageButton exitButton;
    EditText heightEntry, weightEntry, ageEntry;

    private boolean userInitialized; //for saving if account has already been created (do doesn't go though series of start screens again)

    private String DEFAULT_COLOR = "#FF4081";  //for colour theme
    private String accent;
    TextView titleTextView, cmTextView, kgTextView, yearsTextView;

    private String inputType;

    //sex input dropdown
    Spinner sexInputDropdown;
    ArrayAdapter<CharSequence> dropdownAdapter;
    boolean female, male, other;   //for saving gender

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data_input);

        titleTextView = (TextView) findViewById(R.id.userDataTextView);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        heightEntry = (EditText) findViewById(R.id.userHeightEntry);
        weightEntry = (EditText) findViewById(R.id.userWeightEntry);
        ageEntry = (EditText) findViewById(R.id.userAgeEntry);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings (or to default if new user)

        userInitialized = sp.getBoolean("userInitialized", false); //get if user is new or not (if has gone through start screens or not)

        //get reference to dropdown
        sexInputDropdown = findViewById(R.id.sexInput);
        dropdownAdapter = ArrayAdapter.createFromResource(this, R.array.sex_input_array, R.layout.spinner_layout);
        dropdownAdapter.setDropDownViewResource(R.layout.spinner_layout);
        sexInputDropdown.setAdapter(dropdownAdapter);
        sexInputDropdown.setOnItemSelectedListener(this);
        sexInputDropdown.setSelection(0);

        if (userInitialized) {   //existing user editing from settings
            exitButton.setVisibility(View.VISIBLE); //exit to settings button visible if accessing from settings

            cmTextView = (TextView) findViewById(R.id.cmTextView);  //get unit textviews (so can hide them for each kind of user input)
            kgTextView = (TextView) findViewById(R.id.kgTextView);
            yearsTextView = (TextView) findViewById(R.id.yearsTextView);

            inputType = getIntent().getExtras().getString("input_type");

            if (inputType.equals("height")) {    //if user editing height retroactively hide all other textviews/edittexts
                titleTextView.setText("Update Height");
                heightEntry.setHint(Integer.toString(sp.getInt("height", 0)));       //set hint value to current data
                heightEntry.setVisibility(View.VISIBLE);    //set relevant visible
                cmTextView.setVisibility(View.VISIBLE);

                weightEntry.setVisibility(View.GONE);   //hide irrelevant
                kgTextView.setVisibility(View.GONE);
                ageEntry.setVisibility(View.GONE);
                yearsTextView.setVisibility(View.GONE);
                sexInputDropdown.setVisibility(View.GONE);
            } else if (inputType.equals("weight")) {    //if user editing weight retroactively hide all other textviews/edittexts
                titleTextView.setText("Update Weight");
                weightEntry.setHint(Integer.toString(sp.getInt("weight", 0)));    //set hint value to current data
                weightEntry.setVisibility(View.VISIBLE);    //set relevant visible
                kgTextView.setVisibility(View.VISIBLE);

                heightEntry.setVisibility(View.GONE);   //hide irrelevant
                cmTextView.setVisibility(View.GONE);
                ageEntry.setVisibility(View.GONE);
                yearsTextView.setVisibility(View.GONE);
                sexInputDropdown.setVisibility(View.GONE);
            } else if (inputType.equals("age")) {    //if user editing age retroactively hide all other textviews/edittexts
                titleTextView.setText("Update Age");
                ageEntry.setHint(Integer.toString(sp.getInt("age", 0)));    //set hint value to current data
                ageEntry.setVisibility(View.VISIBLE);    //set relevant visible
                yearsTextView.setVisibility(View.VISIBLE);

                heightEntry.setVisibility(View.GONE);   //hide irrelevant
                cmTextView.setVisibility(View.GONE);
                weightEntry.setVisibility(View.GONE);   //hide irrelevant
                kgTextView.setVisibility(View.GONE);
                sexInputDropdown.setVisibility(View.GONE);
            } else if (inputType.equals("sex")) {    //if user editing sex retroactively hide all other textviews/edittexts
                titleTextView.setText("Update Sex");
                String sex = sp.getString("sex", "other");  //set value to current selection
                if (sex.equals("female")) {
                    sexInputDropdown.setSelection(0);
                    female = true;
                    male = false;
                    other = false;
                } else if (sex.equals("male")) {
                    sexInputDropdown.setSelection(1);
                    male = true;
                    female = false;
                    other = false;
                } else if (sex.equals("other")) {
                    sexInputDropdown.setSelection(2);
                    other = true;
                    female = false;
                    male = false;
                }
                sexInputDropdown.setVisibility(View.VISIBLE);

                heightEntry.setVisibility(View.GONE);   //hide irrelevant
                weightEntry.setVisibility(View.GONE);
                cmTextView.setVisibility(View.GONE);
                kgTextView.setVisibility(View.GONE);
                ageEntry.setVisibility(View.GONE);
                yearsTextView.setVisibility(View.GONE);
            }
        } else {   //new user
            exitButton.setVisibility(View.INVISIBLE); //exit button not visible if filling in data for first time
        }
    }

    //save user height, weight, age in sharedprefs
    public void saveUserData() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPrefs.edit();

        //putting data in with key value pairs
        if (userInitialized) {   //if existing user only save what they edited

            if (inputType.equals("height")) {
                boolean edited = !heightEntry.getText().toString().equals("");
                if (edited) {
                    editor.putInt("height", Integer.parseInt(heightEntry.getText().toString()));
                }
            } else if (inputType.equals("weight")) {
                boolean edited = !weightEntry.getText().toString().equals("");
                if (edited) {
                    editor.putInt("weight", Integer.parseInt(weightEntry.getText().toString()));
                }
            } else if (inputType.equals("age")) {
                boolean edited = !ageEntry.getText().toString().equals("");
                if (edited) {
                    editor.putInt("age", Integer.parseInt(weightEntry.getText().toString()));
                }
            } else if (inputType.equals("sex")) {
                if (female) {
                    editor.putString("sex", "female");
                } else if (male) {
                    editor.putString("sex", "male");
                } else if (other) {
                    editor.putString("sex", "other");
                }
            }
        } else {   //if not initialized save all
            boolean heightEdited = !heightEntry.getText().toString().equals("");
            boolean weightEdited = !weightEntry.getText().toString().equals("");
            boolean ageEdited = !ageEntry.getText().toString().equals("");
            if (heightEdited && weightEdited && ageEdited) {  //if data is entered save
                editor.putInt("height", Integer.parseInt(heightEntry.getText().toString()));
                editor.putInt("weight", Integer.parseInt(weightEntry.getText().toString()));
                editor.putInt("age", Integer.parseInt(ageEntry.getText().toString()));
                if (female) {
                    editor.putString("sex", "female");
                } else if (male) {
                    editor.putString("sex", "male");
                } else if (other) {
                    editor.putString("sex", "other");
                }
                gotoActivityGoal();
            } else {   //catch when not edited/added
                noInputDialog();
            }

        }

        editor.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:
                if (!userInitialized) {  //if new user go to next data screen
                    saveUserData();

                } else {   //if existing user go back to settings
                    saveUserData();
                    gotoSettings();
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to settings without saving anything
                gotoSettings();
                break;
        }
    }

    //respond to dropdown selections
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        switch (position) {
            case 0: //first item selected (female)
                female = true;
                male = false;
                other = false;
                break;

            case 1: //second item selected (male)
                female = false;
                male = true;
                other = false;
                break;

            case 2: //third item selected (prefer not to say)
                female = false;
                male = false;
                other = true;
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    //sends new user to next user data screen
    private void gotoActivityGoal() {
        Intent intent = new Intent(this, ActivityGoalActivity.class);
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

    // make dialog come up when no input added
    public void noInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Please enter user data");
        builder.setMessage("User data is necessary to calculate steps, distance traveled, pace and calories burned.");
        //user can click away to close box
        builder.setCancelable(true);

        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(accent));
        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(accent));
    }


}