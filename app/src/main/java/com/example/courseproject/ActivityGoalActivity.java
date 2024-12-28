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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityGoalActivity extends AppCompatActivity implements View.OnClickListener
{

    Button saveButton;
    ImageButton exitButton;
    EditText userGoalEntry;

    public static final String DEFAULT_COLOR = "#FF4081";  //for colour theme
    private String accent;
    TextView titleTextView;

    private boolean userInitialized; //for saving if account has already been created (do doesn't go though series of start screens again)

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        titleTextView = (TextView) findViewById(R.id.activityGoalTitleTextView);

        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        userGoalEntry = (EditText) findViewById(R.id.userGoalEntry);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings

        userInitialized = sp.getBoolean("userInitialized", false); //get if user is new or not (if has gone through start screens or not)
        if (userInitialized) {
            exitButton.setVisibility(View.VISIBLE); //exit to settings button visible if accessing from settings
            userGoalEntry.setHint(Integer.toString(sp.getInt("goal", 0)));    //put current goal in edittext
        }
        else{
            exitButton.setVisibility(View.INVISIBLE); //exit button not visible if filling in data for first time
        }
    }

    public void saveUserData()
    {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPrefs.edit();

        boolean newGoal = !userGoalEntry.getText().toString().equals("");   //check if data has been entered (so no blank field)

        if (newGoal){  //save new goal
            String goalValue= userGoalEntry.getText().toString();   //getting edittext data and converting to int
            int finalGoalValue = Integer.parseInt(goalValue);

            //putting data in with key value pairs
            editor.putInt("goal", finalGoalValue);
            gotoCalorieCount(); //go to next activity
        }
        else{   //if no new goal entered
            if (!userInitialized){  //if new user creating an account set a default goal
                noInputDialog();
            }
        }

        editor.commit();
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())   //check what button is pressed
        {
            case R.id.saveButton:
                if (!userInitialized){   //first time user is creating account so go to next data screen
                    saveUserData();
                }
                else{
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
    private void gotoCalorieCount()
    {
        Intent intent = new Intent(this, CalorieCountActivity.class);
        startActivity(intent);
    }

    //sends existing user to settings
    private void gotoSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs)
    {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }

    // make dialog come up when no input added
    public void noInputDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Please set activity goal.");
        builder.setMessage("Lack of activity goal will prevent use of all RunItUp functionalities. You can change your activity goal at any time in user settings.");
        //user can click away to close box
        builder.setCancelable(true);

        builder.setPositiveButton("Set goal", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }});

        // create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}