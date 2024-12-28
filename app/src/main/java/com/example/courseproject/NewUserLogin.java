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
import android.widget.TextView;

public class NewUserLogin extends AppCompatActivity implements View.OnClickListener {

    private TextView usernameEntry, passwordEntry;
    private Button loginButton;

    public static final String DEFAULT_COLOR = "#FF4081";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_login);

        usernameEntry = (EditText) findViewById(R.id.userAgeEntry);
        passwordEntry = (EditText) findViewById(R.id.contentEntry);

        loginButton = (Button) findViewById(R.id.newUserLoginButton);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        createAccount();
    }

    //save username and password entry to sharedprefs
    public void createAccount() {
        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPrefs.edit();

        sharedPrefs.edit().clear().commit(); //clear old shared prefs

        if (!usernameEntry.getText().toString().equals("") && !passwordEntry.getText().toString().equals("")){  //if user entered username and password
            //putting data in with key value pairs
            editor.putString("username", usernameEntry.getText().toString());
            editor.putString("password", passwordEntry.getText().toString());
            editor.commit();
            gotoUserDataInput();
        }
        else{
            errorDialog();
        }


    }

    private void gotoUserDataInput() {
        Intent userDataInputIntent = new Intent(this, UserDataInputActivity.class);
        startActivity(userDataInputIntent);
    }

    // make dialog come up when no input added
    public void errorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Missing input");
        builder.setMessage("Please input username and password to create new account.");
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
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(DEFAULT_COLOR));
        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(DEFAULT_COLOR));
    }



}