package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener {

    //right version
    TextView titleTextView, nextRunDateTextView, nextRunTimeTextView, nextRunMessageTextView, nextRunDistanceTextView, stepsTextView, kmTextView, activityTimeTextView, caloriesTextView, caloriesLabel, activityProgressLabel;
    ProgressBar activityProgressBar;

    ImageButton settingsButton, hotIcon, mildIcon, coldIcon;

    BottomNavigationView nav;

    LinearLayout runLogLayout;

    //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    public static final String DEFAULT_LIGHT = "#FF9F9F";
    String accent;

    boolean calorieToggle, locationToggle;  //for if calories and locaiton access are turned on

    private String weatherPreference;   //for saved weather preference
    private static final String HOT = "Hot"; //temperature preference constants
    private static final String MILD = "Mild";
    private static final String COLD = "Cold";
    private static final String NO_PREF = "No Preference";
    private static final int DEFAULT_TEMP = -40;
    private double temperature = DEFAULT_TEMP; //for saving temperature value from network
    private String url, result; //for url of weather site and network result

    private boolean connected = false;  //flag for if connected to network (set in checkCOnnection())

    private double latitude, longitude; //for saving coordinates to check current weather nearby

    //for accessing location
    private FusedLocationProviderClient fusedLocationClient;

    public static final String DEFAULT = "not available";
    public static final int INT_DEFAULT = -1;

    //for stats database access
    private StatsDatabase sdb;
    private StatsDatabaseHelper statsDatabaseHelper;
    Cursor cursor;

    //for run database access
    private RunDatabase rdb;
    private RunDatabaseHelper runDatabaseHelper;
    Cursor runCursor;

    //variables for saving last 7 run stats
    private int totalActivityTime, totalSteps;
    private double totalKm, totalCalories; //changed from int

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        checkConnection();  //set flag saying if connected to network
        getCurrentLocation();

        nav = findViewById(R.id.bottomNav);
        nav.setOnItemSelectedListener(this);
        nav.getMenu().findItem(R.id.nav_home).setChecked(true);  //set correct icon checked

        titleTextView = (TextView) findViewById(R.id.dashWelcomeTextView);
        nextRunDateTextView = (TextView) findViewById(R.id.nextRunDateTextView);
        nextRunTimeTextView = (TextView) findViewById(R.id.nextRunTimeTextView);
        nextRunDistanceTextView = (TextView) findViewById(R.id.nextRunDistanceTextView);
        nextRunMessageTextView = (TextView) findViewById(R.id.nextRunMessageTextView);
        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
        kmTextView = (TextView) findViewById(R.id.kmTextView);
        activityTimeTextView = (TextView) findViewById(R.id.timeTextView);
        caloriesTextView = (TextView) findViewById(R.id.caloriesTextView);
        caloriesLabel = (TextView) findViewById(R.id.caloriesLabel);
        activityProgressLabel = (TextView) findViewById(R.id.activityProgressLabel);

        activityProgressBar = (ProgressBar) findViewById(R.id.activityProgressBar);

        runLogLayout = (LinearLayout) findViewById(R.id.runLogLayout);
        runLogLayout.setOnClickListener(this);

        settingsButton = (ImageButton) findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(this);

        hotIcon = (ImageButton) findViewById(R.id.hotIcon);
        hotIcon.setOnClickListener(this);

        mildIcon = (ImageButton) findViewById(R.id.mildIcon);
        mildIcon.setOnClickListener(this);

        coldIcon = (ImageButton) findViewById(R.id.coldIcon);
        coldIcon.setOnClickListener(this);

        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sp);  //update color theme based on settings

        calorieToggle = sp.getBoolean("calorieCount", false);      //get if calorie calculator is turned on or off
        locationToggle = sp.getBoolean("locationAccess", false);      //get if location access is turned on or off

        weatherPreference = sp.getString("weatherPreference", NO_PREF); //get weather preference

        hotIcon.setVisibility(View.GONE);   //set icons hidden by default (only turn on if location/weatherpref is on
        mildIcon.setVisibility(View.GONE);
        coldIcon.setVisibility(View.GONE);

        if (locationToggle && connected) {    //if location is on and connected to network
            mildIcon.setVisibility(View.VISIBLE);
            getCurrentLocation();       //get current location coordinates

            updateWeather(weatherPreference); //get weather from network and update icons
        }


        //get username and password saved in sharedprefs
        String savedUsername = sp.getString("username", DEFAULT);

        //if saved username has content
        if (!savedUsername.equals(DEFAULT)) {
            titleTextView.setText(savedUsername + "'s Dashboard"); //personalized activity title
        }

        sdb = new StatsDatabase(this);
        statsDatabaseHelper = new StatsDatabaseHelper(this);    //accessing stats database

        rdb = new RunDatabase(this);
        runDatabaseHelper = new RunDatabaseHelper(this);    //accessing run database

        totalActivityTime = 0;
        totalSteps = 0;
        totalKm = 0;
        totalCalories = 0;

        try {
            updateStats(sp);   //update stats with SQLite database data
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            updateNextRun();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


    //check when next run is to display data (on next run card)
    private void updateNextRun() throws ParseException {

        List<Date> futureRunDates = new ArrayList<Date>();

        runCursor = rdb.getRunDates();
//
        if (runCursor.getCount() > 0 && runCursor != null) {
            runCursor.moveToFirst();

            int dateIndex = runCursor.getColumnIndex(RunConstants.DATE);

            while (!runCursor.isAfterLast()) {
                Date date = new SimpleDateFormat("dd-MM-yyyy").parse(runCursor.getString(dateIndex));   //get future run date

                if (date.after(new Date())) {    //if date is after current date
                    futureRunDates.add(date);
                } else if (date.before(new Date())) {   //delete scheduled run if date has passed
                    LocalDate localDateNextRun = convertToLocalDateViaInstant(date); //convert date object to local date (no time)

                    int year = localDateNextRun.getYear();
                    int month = localDateNextRun.getMonthValue();
                    int day = localDateNextRun.getDayOfMonth();

                    //format date into consistent order dd-mm-yyyy (what it is in sqlite)
                    String rightDateFormat = Integer.toString(day) + "-" + Integer.toString(month) + "-" + Integer.toString(year);

                    if (rightDateFormat.charAt(1) == '-') rightDateFormat = "0" + rightDateFormat;
                    if (rightDateFormat.charAt(4) == '-')
                        rightDateFormat = rightDateFormat.substring(0, 3) + "0" + rightDateFormat.substring(3);

                    rdb.deleteRun(rightDateFormat.toString());  //delete entries with dd-mm-yyyy date before today (scheduled runs that have passed) in sqlite rundatabase
                }

                runCursor.moveToNext();

            }
        }
        if (futureRunDates.size() > 0) {    //if there are future runs scheduled
            Date nearestDate = getNearestDate(futureRunDates, new Date());  //find run closest to today

            LocalDate localDateNextRun = convertToLocalDateViaInstant(nearestDate); //convert date object to local date (no time)

            int year = localDateNextRun.getYear();
            int month = localDateNextRun.getMonthValue();
            int day = localDateNextRun.getDayOfMonth();

            //format date into consistent order dd-mm-yyyy and update textview
            String rightDateFormat = Integer.toString(day) + "-" + Integer.toString(month) + "-" + Integer.toString(year);

            if (rightDateFormat.charAt(1) == '-') rightDateFormat = "0" + rightDateFormat;
            if (rightDateFormat.charAt(4) == '-')
                rightDateFormat = rightDateFormat.substring(0, 3) + "0" + rightDateFormat.substring(3);

            //write date in easier form for human reading
            try {
                Date runDate = new SimpleDateFormat("dd-MM-yyyy").parse(rightDateFormat);

                SimpleDateFormat dateWords = new SimpleDateFormat("dd MMMM yyyy");
                String dateWordsString = (dateWords.format(runDate));

                nextRunDateTextView.setText(dateWordsString);
                findSelectedRunByDate(rightDateFormat.toString());  //update distance, time, message textviews with next run data

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //convert date to local date (remove time)
    public LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    //find selected run in arraylist by date
    private void findSelectedRunByDate(String date) {
        Cursor selectedRunCursor = rdb.getData();

        //get data indeces
        int dateIndex = selectedRunCursor.getColumnIndex(RunConstants.DATE);
        int timeIndex = selectedRunCursor.getColumnIndex(RunConstants.TIME);
        int distanceIndex = selectedRunCursor.getColumnIndex(RunConstants.DISTANCE);
        int messageIndex = selectedRunCursor.getColumnIndex(RunConstants.MESSAGE);

        selectedRunCursor.moveToFirst();

        while (!selectedRunCursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
            String nextRunDate = selectedRunCursor.getString(dateIndex);

            if (nextRunDate.equals(date)) { //update distance, time and message inputs with scheduled run data
                String nextRunTime24h = selectedRunCursor.getString(timeIndex);

                //convert time to 12 h with am/pm
                try {
                    SimpleDateFormat time24h = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat time12h = new SimpleDateFormat("hh:mm a");
                    Date nextRunTimeDate = time24h.parse(nextRunTime24h);
                    String nextRunTime12h = (time12h.format(nextRunTimeDate));
                    nextRunTimeTextView.setText(nextRunTime12h);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                nextRunDistanceTextView.setText(selectedRunCursor.getString(distanceIndex) + " km");
                nextRunMessageTextView.setText(selectedRunCursor.getString(messageIndex));
                break;
            }
            selectedRunCursor.moveToNext();    //go to next row (if there is one)
        }
    }


    //find closest date to current date
    //https://stackoverflow.com/questions/7128704/how-would-i-go-about-finding-the-closest-date-to-a-specified-date-java
    public static Date getNearestDate(List<Date> dates, Date currentDate) {
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        for (Date date : dates) {
            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                minDiff = diff;
                minDate = date;
            }
        }
        return minDate;
    }


    private void updateStats(SharedPreferences sp) throws ParseException {
        cursor = sdb.getRunDataDashboard(); //method to get latest 7 entries

        cursor.moveToFirst();

        //get data for recyclerview
        int dateIndex = cursor.getColumnIndex(StatsConstants.DATE);
        int activityTimeIndex = cursor.getColumnIndex(StatsConstants.TIME);
        int stepIndex = cursor.getColumnIndex(StatsConstants.STEPS);
        int kmIndex = cursor.getColumnIndex(StatsConstants.KM);
        int caloriesIndex = cursor.getColumnIndex(StatsConstants.CALORIES);

        ArrayList<String> runLogChronological = new ArrayList<String>();

        while (!cursor.isAfterLast()) {
            String runDate = cursor.getString(dateIndex);
            String runLength = Integer.toString(cursor.getInt(activityTimeIndex));
            String runSteps = Integer.toString(cursor.getInt(stepIndex));
            String runDistance = Double.toString(cursor.getDouble(kmIndex));    //changed from int
            String runCalories = Double.toString(cursor.getDouble(caloriesIndex));    //changed from int

            String s = runDate + "," + runLength + "," + runSteps + "," + runDistance + "," + runCalories;
            runLogChronological.add(s);
            cursor.moveToNext();    //go to next row (if there is one)
        }

        //Sort entries from most to least recent
        String[] results;
        int mindex;
        Date minDate;

        for (int i = 0; i < runLogChronological.size(); i++) {
            results = (runLogChronological.get(i).toString()).split(","); //split results using ,
            minDate = new SimpleDateFormat("dd-MM-yyyy").parse(results[0]);
            mindex = i;

            for (int j = i + 1; j < runLogChronological.size(); j++) {
                String[] dateRunArray = (runLogChronological.get(j).toString()).split(","); //split results using ,
                Date dateToCompare = new SimpleDateFormat("dd-MM-yyyy").parse(dateRunArray[0]);   //get future run date

                if (dateToCompare.after(minDate)) {    //if date is after current date
                    minDate = dateToCompare;
                    mindex = j;
                }
            }
            Collections.swap(runLogChronological, mindex, i);
        }

        //get data from last week for stats overview
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusWeeks(1);

        for (int i = 0; i < runLogChronological.size(); i++) { //loop through all, fill each with stat data
            String[] runStatsArray = (runLogChronological.get(i).toString()).split(","); //split results using ,
            LocalDate resultDate = LocalDate.parse(runStatsArray[0], DateTimeFormatter.ofPattern("dd-MM-yyyy"));   //get localdate of specific run

            if (resultDate.isAfter(weekAgo)) {   //if result is in last week, add to totals
                int runActivityTime = Integer.parseInt(runStatsArray[1]);
                totalActivityTime += runActivityTime;

                int runSteps = Integer.parseInt(runStatsArray[2]);
                totalSteps += runSteps;

                double runDistance = Double.parseDouble(runStatsArray[3]);
                totalKm += runDistance;

                double runCalories = Double.parseDouble(runStatsArray[4]);
                totalCalories += runCalories;
            }
        }

        // format seconds into hours, minutes, seconds
        int hours = totalActivityTime / 3600;
        int minutes = (totalActivityTime % 3600) / 60;
        int secs = totalActivityTime % 60;
        String formattedTime = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

        activityTimeTextView.setText(formattedTime);    //setting textviews to display stats

        DecimalFormat df = new DecimalFormat("###.##");   //round to 2 decimal places
        String kmFormatted = df.format(totalKm);
        String caloriesFormatted = df.format(totalCalories);

        kmTextView.setText(kmFormatted);
        stepsTextView.setText(Integer.toString(totalSteps));
        caloriesTextView.setText(caloriesFormatted);

        //if calories turned off do not display calories burned stat
        if (!calorieToggle) {
            caloriesTextView.setVisibility(View.GONE);
            caloriesLabel.setVisibility(View.GONE);
        } else {
            caloriesTextView.setVisibility(View.VISIBLE);
            caloriesLabel.setVisibility(View.VISIBLE);
        }

        //update activity goal progressbar
        int activityGoal = sp.getInt("goal", INT_DEFAULT);
        if (activityGoal != INT_DEFAULT && activityGoal != 0) {
            activityProgressBar.setMax(activityGoal);
            activityProgressBar.setProgress((int) totalKm);
            activityProgressLabel.setText((int) totalKm + "km / " + activityGoal + "km");
        } else {
            activityProgressBar.setMax(activityGoal);
            activityProgressBar.setProgress(0);
            activityProgressLabel.setText("Set activity goal to view progress");
        }
    }


    //react to button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {
            case R.id.hotIcon:
                messageDialog("It's hot near you", "The local temperature is over 20 degrees");
                break;

            case R.id.mildIcon:
                messageDialog("It's mild near you", "The local temperature is over between 5 and 20 degrees");
                break;

            case R.id.coldIcon:
                messageDialog("It's cold near you", "The local temperature is below 5 degrees");
                break;

            case R.id.settingsButton:
                gotoSettings();
                break;

            case R.id.runLogLayout:
                gotoRunSchedule(); //go to all scheduled runs page

                break;

            default:
//                Toast.makeText(this, "unknown button id", Toast.LENGTH_SHORT).show();
        }
    }

    //explicit intents for transitioning to other app screens from navigation
    private void gotoJournal() {
        Intent journalIntent = new Intent(this, JournalActivity.class);
        startActivity(journalIntent);
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

    private void gotoRunSchedule() {
        Intent i = new Intent(this, FutureRunScheduleActivity.class);
        startActivity(i);
    }

    //to give general messages (in this case used for weather)
    public void messageDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);  //create alert with custom styling

        builder.setTitle(title);
        builder.setMessage(message);

        //user can click away to close box
        builder.setCancelable(true);

        //if user presses yes cancel run without saving
        builder.setPositiveButton(
                "OKAY",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create and show alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(accent));  //set buttons to theme colour
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(accent));
    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR); //get color theme preference
        String accentLight = sharedPrefs.getString("themeColorLight", DEFAULT_LIGHT);

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        stepsTextView.setTextColor(Color.parseColor(accent));
        kmTextView.setTextColor(Color.parseColor(accent));
        caloriesTextView.setTextColor(Color.parseColor(accent));
        activityTimeTextView.setTextColor(Color.parseColor(accent));
        nextRunDateTextView.setTextColor(Color.parseColor(accent));

        activityProgressBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor(accentLight))); //set progress bar colour to lighter accent
    }

    //bottom navigation bar
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_stats:
                gotoStats();
                break;

            case R.id.nav_run:
                gotoAddRun();
                break;

            case R.id.nav_recovery:
                gotoRecovery();
                break;

            case R.id.nav_journal:
                gotoJournal();
                break;
        }
        return true;
    }

    private void updateWeather(String weatherPreference) {

        Thread myThread = new Thread(new GetWeatherThread());
        myThread.start();
    }


    //get current location of user to get weather information from
    private void getCurrentLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "no permission granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                if (location != null) { //check if location is null
//                    Toast.makeText(DashboardActivity.this, "got location", Toast.LENGTH_SHORT).show();

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                } else {
//                    Toast.makeText(DashboardActivity.this, "no location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    public void checkConnection() {
        ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) { //update flag based on connection
            connected = true;
        } else {
            connected = false;
        }
    }

    private class GetWeatherThread implements Runnable {
        @Override
        public void run() {

            try {
                Thread.sleep(200);  //thread has to delay to give the current location time to load
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //update url with new longitude and latitude (according to current loaction coordinates)
            url = "http://api.geonames.org/findNearByWeatherJSON?lat=" +
                    latitude + "&lng=" +
                    longitude + "&username=osaa";

            getWeather();   //get json object from network, parse, update ui accordingly
        }
    }

    //get json object from network, parse, update ui accordingly
    private void getWeather() {

        try {      //access network data
            result = readJSONData(url);
        } catch (Exception e) {
            Log.d("issue first catch", e.getLocalizedMessage());
        }

        try {   //parse json object to get temperature value double
            JSONObject jsonObject = new JSONObject(result);
            JSONObject weatherObservationItems = new JSONObject(jsonObject.getString("weatherObservation"));

            temperature = Double.parseDouble(weatherObservationItems.getString("temperature"));

            Log.d("Temperature", String.valueOf(temperature));

        } catch (Exception e) {
            Log.d("issue second catch", e.getLocalizedMessage());
        }


        this.runOnUiThread(new Runnable() { //update ui icons and dialog box according to temperature and user preferences
            @Override
            public void run() {

                if (!weatherPreference.equals(NO_PREF) && temperature != DEFAULT_TEMP) {    //give a message on entry if weather pref matches current weather
                    if (weatherPreference.equals(HOT) && temperature >= 20) {
                        messageDialog("It's your favourite temperature to run!", "It's hot near you, time to get out for a run");
                    } else if (weatherPreference.equals(MILD) && temperature < 20 && temperature >= 5) {
                        messageDialog("It's your favourite temperature to run!", "It's mild near you, time to get out for a run");
                    } else if (weatherPreference.equals(COLD) && temperature < 5) {
                        messageDialog("It's your favourite temperature to run!", "It's cold near you, time to get out for a run");
                    }
                }

                if (temperature != DEFAULT_TEMP) {   //if temperature was updated display appropriate icon

                    if (temperature >= 20) { //set icon to match temperature
                        hotIcon.setVisibility(View.VISIBLE);
                        mildIcon.setVisibility(View.GONE);
                        coldIcon.setVisibility(View.GONE);

                    } else if (temperature < 20 && temperature >= 5) {
                        mildIcon.setVisibility(View.VISIBLE);
                        hotIcon.setVisibility(View.GONE);
                        coldIcon.setVisibility(View.GONE);

                    } else if (temperature < 5) {
                        coldIcon.setVisibility(View.VISIBLE);
                        hotIcon.setVisibility(View.GONE);
                        mildIcon.setVisibility(View.GONE);
                    }
                }
            }
        });
    }



    private String readJSONData(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 2500;

        URL url = new URL(myurl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("tag", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
                conn.disconnect();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }


}
