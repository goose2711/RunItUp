package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class InputRunActivity extends AppCompatActivity implements View.OnClickListener {

    RunDatabase rdb;
    StatsDatabase sdb;
    JournalDatabase jdb;

    Button saveButton;
    ImageButton exitButton;
    TextView titleTextView, kmTextView;

    ToggleButton ampmToggle;
    boolean pm = false;

    private EditText runTimeEntry, runDistanceEntry, messageEntry;  //for future run scheduling
    private EditText stepsEntry, kmEntry, activityTimeEntry, caloriesEntry;  //for past run entry
    private boolean pastRun;    //for identifying if future of past run being input

    String accent;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";

    String inputType, runDate;   //for checking if past or future run entered
    String futureRunTime;  //time future run is scheduled at (for making Date object to get difference between in millis for reminder)

    boolean calorieToggle, runRemindersToggle;  //for accessing user preference toggles from sharedpreferences

    int intentsSentNum;

    //variables for user data (for calculation of stats)
    private double averageStrideLength;
    private static final double strideLengthVariation = 0.05; // Variation in stride length as a decimal percentage
    int weight, height, age;
    String sex;
    double caloriesPerStep;

    String previousActivity;    //for going back to correct activity if exited


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_run);

        previousActivity = getIntent().getExtras().getString("prevActivity");   //get activity that started this activity

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);

        ampmToggle = (ToggleButton) findViewById(R.id.ampmToggle);
        ampmToggle.setOnCheckedChangeListener(checkChangeListener);

        titleTextView = (TextView) findViewById(R.id.inputRunTitleTextView);  //for color change with theme
        kmTextView = (TextView) findViewById(R.id.kmTextView);  //for hiding textview when past run entry

        messageEntry = (EditText) findViewById(R.id.messageEntry);    //for changing message content based on past or future run being input

        runTimeEntry = (EditText) findViewById(R.id.runTimeEntry);  //for saving future run details
        runDistanceEntry = (EditText) findViewById(R.id.runDistanceEntry);

        stepsEntry = (EditText) findViewById(R.id.stepsEntry);  //for saving past run details
        kmEntry = (EditText) findViewById(R.id.kmEntry);
        activityTimeEntry = (EditText) findViewById(R.id.activityTimeEntry);
        caloriesEntry = (EditText) findViewById(R.id.caloriesEntry);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        calorieToggle = sp.getBoolean("calorieCount", false);      //get if calorie calculator is turned on or off
        runRemindersToggle = sp.getBoolean("runScheduling", false);      //get if run reminders are turned on or off

        if (runRemindersToggle) {   //if run reminders are on
            createNotificationChannel();    //create notification channel for future run notifications
            intentsSentNum = sp.getInt("intentNumber", 0); //get how many notification intents have been sent
        }

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sharedPrefs);  //update color theme based on settings

        inputType = getIntent().getExtras().getString("input_type");
        if (inputType.equals("past")) {  //if user inputing past run retroactively
            pastRun = true;
            titleTextView.setText("Input Past Run");
            saveButton.setText("ADD RUN");
            messageEntry.setHint("How did the run go? How did you feel? What positive aspects and traits can you highlight?");

            runTimeEntry.setVisibility(View.GONE);  //remove future run edittexts
            runDistanceEntry.setVisibility(View.GONE);
            kmTextView.setVisibility(View.GONE);
            ampmToggle.setVisibility(View.GONE);

            stepsEntry.setVisibility(View.VISIBLE);  //show past run edittexts
            kmEntry.setVisibility(View.VISIBLE);
            activityTimeEntry.setVisibility(View.VISIBLE);

            //if calories turned off do not display calories burned stat
            if (!calorieToggle) {
                caloriesEntry.setVisibility(View.GONE);
            } else {
                caloriesEntry.setVisibility(View.VISIBLE);
            }

            //Retrieve the users Age, Height, Weight, sex from Shared Preferences (for calculating run stats based on each other and user data)
            weight = sp.getInt("weight", 0);
            height = sp.getInt("height", 0);
            age = sp.getInt("age", 0);
            sex = sp.getString("sex", "other");

            //initialize average stride length based on sex input
            if (sex.equals("male")) {
                averageStrideLength = 0.762; // Average stride length in meters for men
            } else if (sex.equals("female")) {
                averageStrideLength = 0.664; // Average stride length in meters for women
            } else {   //if sex is "other" (not specified by user)
                averageStrideLength = 0.731; // Average stride length in meters for humans overall
            }
            caloriesPerStep = weight * 0.00059756;


        } else if (inputType.equals("future")) {    //if user scheduling new run
            pastRun = false;
            titleTextView.setText("Schedule Future Run");
            saveButton.setText("SCHEDULE RUN");
            messageEntry.setHint("Set a reminder message to give your future self some encouragement");

            stepsEntry.setVisibility(View.GONE);  //remove past run edittexts
            kmEntry.setVisibility(View.GONE);
            activityTimeEntry.setVisibility(View.GONE);
            caloriesEntry.setVisibility(View.GONE);

            runTimeEntry.setVisibility(View.VISIBLE);  //show future run edittexts
            runDistanceEntry.setVisibility(View.VISIBLE);
            kmTextView.setVisibility(View.VISIBLE);
            ampmToggle.setVisibility(View.VISIBLE);
        }

        runDate = getIntent().getExtras().getString("run_date");   //get selected date for run

        rdb = new RunDatabase(this);    //initialize instance of database to access run schedule
        sdb = new StatsDatabase(this);    //initialize instance of database to save past run stats
        jdb = new JournalDatabase(this);    //initialize instance of database to save past run journal entry
    }

    //inner class for togglebutton check change listener
    private CompoundButton.OnCheckedChangeListener checkChangeListener = new CompoundButton.OnCheckedChangeListener() {

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                pm = true;
            } else {
                pm = false;
            }
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.saveButton:   //save run into run log and go back to run activity
                //SAVE RUN DATE/GOAL/TIME DATA INTO RUN LOG (maybe just save date and give day reminders?)

                if (pastRun) {     //past run being input
                    try {
                        savePastRun();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {   //future run being scheduled
                    try {
                        saveFutureRunInput();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.exitButton:   //if exit button is pressed return to previous activity without saving anything
                switch (previousActivity) {
                    case "future_run_schedule_activity":    //if came from run scheduling calendar return there
                        gotoFutureRunSchedule();
                        break;

                    case "add_run_activity":
                        gotoAddRun();  //if came from add run activity return there
                        break;
                }
                break;
        }
    }

    //save future run scheduled
    public void saveFutureRunInput() throws ParseException {
        String time = runTimeEntry.getText().toString();
        String distance = runDistanceEntry.getText().toString();
        String runMessage = messageEntry.getText().toString();

        boolean validTime = false;

        if (!time.equals("") && !distance.equals("") && !runMessage.equals("")) { //only save the data if fields have data

            //check time is formatted correctly
            if (time.length() > 1 && time.charAt(1) == ':' || time.length() > 2 && time.charAt(2) == ':') { 

                if (time.charAt(1) == ':') {     //if h:mm format
                    String hrLen = time.substring(0, 1);
                    String mnLen = time.substring(2);
                    if (mnLen.length() > 2) {   //invalid time input
                        errorDialog("Invalid time entry", "Please format times as h:mm or hh:mm only");
                        validTime = false;
                    } else {
                        //valid time input
                        validTime = true;
                    }
                }

                if (time.charAt(2) == ':') {     //if hh:mm format
                    String hrLen = time.substring(0, 2);
                    String mnLen = time.substring(3);
                    if (mnLen.length() > 2) {   //invalid time input
                        errorDialog("Invalid time entry", "Please format times as h:mm or hh:mm only");
                        validTime = false;
                    } else {
                        //valid time input
                        validTime = true;
                    }
                }
            } else {
                errorDialog("Invalid time entry", "Please format times as h:mm or hh:mm only");
            }

            if (validTime) {    //if time entry is valid then save

                if (!pm) {   //if am left
                    time = time + " AM";
                } else {
                    time = time + " PM";
                }

                if (time.charAt(1) == ':') {    //parse t:tt to tt:tt
                    time = "0" + time;
                }

                try {
                    //parse 12 hour time into 24 hour time
                    futureRunTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mm a", Locale.US)).format(DateTimeFormatter.ofPattern("HH:mm"));
                    //this website was used to convert the 12 hour time to 24 hour time
                    //https://stackoverflow.com/questions/6531632/conversion-from-12-hours-time-to-24-hours-time-in-java

                    int runDistance = Integer.parseInt(distance);

                    long id = rdb.insertData(runDate, runDistance, runMessage, futureRunTime);   //add future run date, distance, message and time to run database

                    //return to previous activity
                    switch (previousActivity) {
                        case "future_run_schedule_activity":    //if came from run scheduling calendar return there
                            gotoFutureRunSchedule();
                            break;

                        case "add_run_activity":
                            gotoRun();  //return to run activity
                            break;

                        case "run_activity":
                            gotoRun();  //return to run activity
                            break;
                    }

                    if (id < 0) {
//                        Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                    }

                    if (runRemindersToggle) {   //if run reminders turned on
                        futureRunNotification();    //make notification of future run
                    }

                } catch (Exception parseError) {
                    errorDialog("Invalid time entry", "Please enter time in 12h format");
                }
            }

        } else {
            errorDialog("Missing input data", "Please enter data in all fields to save");  //error when distance and time not set

        }
    }

    //save data from past run
    public void savePastRun() throws ParseException {

        //add journal entry to journal entries database
        String runMessage = messageEntry.getText().toString();
        if (!runMessage.equals("")) { //only save the data if there is an entry added
            long id = jdb.insertData(runDate, runMessage);   //add current date and entry

            if (id < 0) {
//                Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
            }
        } else {
//            Toast.makeText(this, "no entry added", Toast.LENGTH_SHORT).show();
        }

        //add stats to stats database
        String steps = stepsEntry.getText().toString(); //get steps entry
        String distance = kmEntry.getText().toString(); //get km entry

        double runDistance;
        if (distance.equals("")) {   //if no km distance entered
            if (!steps.equals("")) {  //if steps not blank calculate km from steps
                int runSteps = Integer.parseInt(steps); //get step count
                double strideLength = averageStrideLength * (1 + strideLengthVariation);    //get stride length
                double metersTemp = runSteps * strideLength;  //calculate run distance from steps
                double kmTemp = metersTemp / 1000; //Convert from m to km by dividing the value by 1000

                DecimalFormat df = new DecimalFormat("###.##");   //round km to 2 decimal places
                String kmFormatted = df.format(kmTemp);
                runDistance = Double.parseDouble(kmFormatted);
            } else {   //if steps also blank no data entered
                runDistance = 0;
            }
        } else {    //if steps data entered save
            runDistance = Double.parseDouble(distance);
        }

        //save steps
        int runSteps;
        if (steps.equals("")) { //if no steps input no data saved
            runSteps = 0;
        } else {    //if data input save
            runSteps = Integer.parseInt(steps);
        }

        String calories = caloriesEntry.getText().toString();
        double runCalories;
        if (calories.equals("")) {  //if no calories entered
            if (!steps.equals("")) {  //if steps not blank calculate calories burned from steps
                caloriesPerStep = weight * 0.00059756;
                double caloriesTemp = runSteps * caloriesPerStep;

                DecimalFormat df = new DecimalFormat("###.##");   //round calories to 2 decimal places
                String caloriesFormatted = df.format(caloriesTemp);
                runCalories = Double.parseDouble(caloriesFormatted);
            } else {  //else no data
                runCalories = 0;
            }
        } else { //if there is calorie data parse into double
            runCalories = Double.parseDouble(calories);
        }

        //activity time
        String time = activityTimeEntry.getText().toString();
        int runTime;
        if (time.equals("")) {  //if no time entered no data
            runTime = 0;
        } else {    //else save data in seconds
            runTime = Integer.parseInt(time);
        }
        runTime *= 60;  //get seconds of activity time

        long id = sdb.insertPastRunData(runDate, runTime, runDistance, runCalories, runSteps);   //save run data to run stats database (will show up in recycler)

        if (id < 0) {
//            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        }
        gotoRun();  //return to run activity
    }


    //explicit intents for transitioning to other app screens from navigation
    public void gotoRun() {
        Intent runIntent = new Intent(this, RunActivity.class);
        startActivity(runIntent);
    }

    public void gotoAddRun() {
        Intent runIntent = new Intent(this, AddRunActivity.class);
        startActivity(runIntent);
    }

    private void gotoFutureRunSchedule() {
        Intent intent = new Intent(this, FutureRunScheduleActivity.class);
        intent.putExtra("input_type", "future");
        intent.putExtra("prevActivity", "future_run_schedule_activity"); //what activity started the intent/activity
        startActivity(intent);
    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        saveButton.setBackgroundColor(Color.parseColor(accent));   //set button color to accent color
    }


    //sets notification at time of future run
    public void futureRunNotification() throws ParseException {

        Intent intent = new Intent(this, RunNotificationService.class);
        intent.putExtra("message", messageEntry.getText().toString());
        intent.putExtra("goal", runDistanceEntry.getText().toString());
        intent.putExtra("code", intentsSentNum);
        intent.putExtra("id", intentsSentNum);

       //get difference between dates in millis
        Date now = new Date();  //now

        String futureRunTemp = runDate + "  " + futureRunTime; //future run scheduled date and time
        Date futureRunDateParsed = new SimpleDateFormat("dd-MM-yyyy hh:mm").parse(futureRunTemp);  //get date of future run with time and date

        long millisDifference = futureRunDateParsed.getTime() - now.getTime();  //get millis between now and future date

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(this, intentsSentNum, intent, PendingIntent.FLAG_IMMUTABLE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + millisDifference, pendingIntent);    //actual code (real time delay)
//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent);    //for testing if works (short time delay)

        intentsSentNum += 1;
        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("intentNumber", intentsSentNum);      //add new intent number (this allows correct run message to be displayed with notification
        editor.commit();
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
        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(accent));
        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(accent));
    }


    //I used these resources for learning to create a timed notification
    //https://www.youtube.com/watch?v=4BuRMScaaI4
    //https://developer.android.com/develop/ui/views/notifications/build-notification
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "RunReminderChannel";
            String description = "Channel for reminders at a scheduled run time";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("notifyRun", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


}