package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UsernamePasswordChangeActivity extends AppCompatActivity implements View.OnClickListener {

    TextView titleTextView;
    EditText currentData, newData;
    Button saveButton;
    ImageButton exitButton;

    boolean saveNewData = false;

    String accent;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";

    String dataToChange;

    public static final String DEFAULT = "not available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username_password_change);

        titleTextView = (TextView) findViewById(R.id.titleTextView);

        currentData = (EditText) findViewById(R.id.currentDataEntry);
        newData = (EditText) findViewById(R.id.newDataEntry);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        dataToChange = getIntent().getExtras().getString("dataToChange");

        if (dataToChange.equals("username")) {  //if changing username
            titleTextView.setText("Change Username");
            currentData.setHint("CURRENT USERNAME");
            newData.setHint("NEW USERNAME");
        } else if (dataToChange.equals("password")) {    //if changing password
            titleTextView.setText("Change Password");
            currentData.setHint("CURRENT PASSWORD");
            newData.setHint("NEW PASSWORD");
        }

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings
    }

    //check if data entered is different than existing
    private boolean checkUserData() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        if (dataToChange.equals("username")) {  //if changing username
            String enteredUsername = currentData.getText().toString();
            String savedUsername = sharedPrefs.getString("username", DEFAULT);
            if (enteredUsername.equals(savedUsername)) {    //if username entered matches saved username
                if (!newData.getText().toString().equals("")) {  //if edittext isn't blank
                    saveNewData = true;
                } else {
                    errorDialog();
                }
            } else {
                errorDialog();
            }
        } else if (dataToChange.equals("password")) {  //if changing username
            String enteredPassword = currentData.getText().toString();
            String savedPassword = sharedPrefs.getString("password", DEFAULT);
            if (enteredPassword.equals(savedPassword)) {    //if username entered matches saved username
                if (!newData.getText().toString().equals("")) {  //if edittext isn't blank
                    saveNewData = true;
                } else {
                    errorDialog();
                }
            } else {
                errorDialog();
            }
        }
        return saveNewData;
    }

    //save the data
    public void saveUserData() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        if (checkUserData()) {
            if (dataToChange.equals("username")) {
                editor.putString("username", newData.getText().toString());
            } else if (dataToChange.equals("password")) {
                editor.putString("password", newData.getText().toString());
            }
        }
        editor.commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:
                saveUserData(); //save new data
                if (saveNewData) {  //if there is new data to save (no empty fields)
                    gotoSettings(); //go back to settings
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to settings without saving anything
                gotoSettings();
                break;
        }
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }

    //sends existing user to settings
    private void gotoSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }


    // make dialog come up when no input added
    public void errorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Incorrect credentials");
        builder.setMessage("Please try again");
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