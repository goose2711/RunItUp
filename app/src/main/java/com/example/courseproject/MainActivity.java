package com.example.courseproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String DEFAULT = "not available";

    private Button loginButton, createAccountButton;

    EditText usernameEntry, passwordEntry;

    public static final String DEFAULT_COLOR = "#FF4081";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEntry = (EditText) findViewById(R.id.userAgeEntry);
        passwordEntry = (EditText) findViewById(R.id.contentEntry);

        if (getSharedPreferences("UserData", Context.MODE_PRIVATE) == null) { //go to login for new user if no SharedPreferences
            getSharedPreferences("UserData", Context.MODE_PRIVATE).edit().clear().commit();    //clear all old sharedprefs

            this.deleteDatabase(StatsConstants.TABLE_NAME);
            this.deleteDatabase(RunConstants.TABLE_NAME);
            this.deleteDatabase(JournalConstants.TABLE_NAME);

            gotoNewUserLogin(); //go to new user create account page
        }

        if (getSharedPreferences("UserData", Context.MODE_PRIVATE) != null) { //go to login for new user if no username and password in SharedPreferences
            SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

            String username = sharedPrefs.getString("username", DEFAULT);
            String password = sharedPrefs.getString("password", DEFAULT);

            if (username.equals(DEFAULT) && password.equals(DEFAULT))//if there is no username or password saved
            {
                getSharedPreferences("UserData", Context.MODE_PRIVATE).edit().clear().commit();    //clear all old sharedprefs
                this.deleteDatabase(StatsConstants.TABLE_NAME);
                this.deleteDatabase(RunConstants.TABLE_NAME);
                this.deleteDatabase(JournalConstants.TABLE_NAME);

                gotoNewUserLogin(); //go to new user create account page
            }
        }


        loginButton = (Button) findViewById(R.id.newUserLoginButton);
        loginButton.setOnClickListener(this);

        createAccountButton = (Button) findViewById(R.id.saveButton);
        createAccountButton.setOnClickListener(this);
    }

    //checks username and password entry against data in SharedPreferences
    public void loginCheck() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        //get username and password saved in sharedprefs
        String savedUsername = sharedPrefs.getString("username", DEFAULT);
        String savedPassword = sharedPrefs.getString("password", DEFAULT);

        //get username and password entered
        String enteredUsername = usernameEntry.getText().toString();
        String enteredPassword = passwordEntry.getText().toString();

        //if they match log in to dashboard activity
        if (savedUsername.equals(enteredUsername) && savedPassword.equals(enteredPassword)) {
            gotoDash();
        } else {
            errorDialog("No user found", "Please check your login credentials");
            usernameEntry.setText("");
            passwordEntry.setText("");
        }
    }

    //send user to new user login page
    public void gotoNewUserLogin() {
        Intent newUserLoginIntent = new Intent(this, NewUserLogin.class);
        startActivity(newUserLoginIntent);
    }

    //sends existing user to dashboard
    public void gotoDash() {
        Intent dashIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashIntent);
    }

    //clear data when making new account
    private void clearData() {
        getSharedPreferences("UserData", Context.MODE_PRIVATE).edit().clear().commit();    //clear all old sharedprefs

        JournalDatabaseHelper jdh = new JournalDatabaseHelper(this);    //close and clear sqlite databases
        jdh.close();

        StatsDatabaseHelper sdh = new StatsDatabaseHelper(this);
        sdh.close();

        RunDatabaseHelper rdh = new RunDatabaseHelper(this);
        rdh.close();

        this.deleteDatabase(StatsConstants.DATABASE_NAME);
        this.deleteDatabase(RunConstants.DATABASE_NAME);
        this.deleteDatabase(JournalConstants.DATABASE_NAME);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.newUserLoginButton:  //if login button pressed check user credentials
                loginCheck();
                break;

            case R.id.saveButton:  //if create new account button pressed
                accountDeleteDialog();
                break;
        }
    }

    // make dialog come up when no input added
    public void errorDialog(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);
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
    }

    // make dialog come up when no input added
    public void accountDeleteDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Are you sure you want to create a new account?");
        builder.setMessage("Creating a new account will delete all saved account data.");
        //user can click away to close box
        builder.setCancelable(true);

        builder.setPositiveButton("Create New Account", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                clearData();
                gotoNewUserLogin();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        // create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(DEFAULT_COLOR));
        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(DEFAULT_COLOR));
    }


}

