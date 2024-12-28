package com.example.courseproject;

import static java.lang.Math.abs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurrentRunActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, LocationListener {

    Button pauseButton, finishButton;
    ImageButton exitButton, musicButton;

    TextView titleTextView, timeElapsedTextView, runProgressLabel, stepsTextView, kmTextView, caloriesTextView, caloriesLabel;

    SensorManager sensorManager;    //getting sensor to track running
    Sensor accelerometer;

    String accent, accentLight;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";
    public static final String DEFAULT_LIGHT = "#FF9F9F";

    ProgressBar runProgressBar; //progress bar for run km progress

    boolean paused = true;  //for starting/stopping data collection

    boolean calorieToggle, musicToggle, locationToggle;   //for saving user preference of calories/music connect/location access on/off

    private int seconds;    //for stopwatch
    private int steps;        //variables for run stats
    private double km, calories;  //variable for km (changed from int)

    StatsDatabase sdb;  //database to add stats to

    private String goalInput = "";   //string to save user goal input (from goalinputdialog)
    LinearLayout progressBarCard;   //for hiding or showing progress bar card depending on if user inputs goal

    //calculations variables
    private static final int stepThreshold = 5; // adjust this value to calibrate the step detection
    private float previousAccelMag = 0;

    private double averageStrideLength;
    private static final double strideLengthVariation = 0.05; // Variation in stride length as a decimal percentage

    int weight, height, age;
    String sex;

    double caloriesPerStep;

    //variables for binding to service that continues to get run data when app closed
    CurrentRunDataService dataService;
    boolean bound = false;

    boolean finished = false;   //indicates if finished with activity (for service that calculates stats when not open)

    //variables for map route saving
    List<LatLng> latLngList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_run);
        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        locationToggle = sp.getBoolean("locationAccess", false);    //get if location is on

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        checkLocationPermission();     //check location permissions
        //update location toggle in case permissions changed
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationToggle = true;
        }

        if (locationToggle) {   //if location on get current location
            getCurrentLocation();   //get current location of user to start run route
//            Toast.makeText(this, "permission", Toast.LENGTH_SHORT).show();
        }
        else{
//            Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
        }

        try {
            goalInput = getIntent().getExtras().getString("goal");   //get goal (set if scheduled future run and came here from reminder notification)
        } catch (Exception bundleError) {
            //no goal entered so goal input is null
        }

        pauseButton = (Button) findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(this);

        finishButton = (Button) findViewById(R.id.finishButton);
        finishButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(this);

        musicButton = (ImageButton) findViewById(R.id.musicButton);
        musicButton.setOnClickListener(this);

        runProgressBar = (ProgressBar) findViewById(R.id.runProgressBar);

        timeElapsedTextView = (TextView) findViewById(R.id.timeElapsedTextView);
        runProgressLabel = (TextView) findViewById(R.id.runProgressLabel);
        stepsTextView = (TextView) findViewById(R.id.stepsTextView);
        kmTextView = (TextView) findViewById(R.id.kmTextView);
        caloriesTextView = (TextView) findViewById(R.id.caloriesTextView);
        caloriesLabel = (TextView) findViewById(R.id.caloriesLabel);

        titleTextView = (TextView) findViewById(R.id.currentRunTitleTextView);  //for color change

        progressBarCard = (LinearLayout) findViewById(R.id.progressBarCard);

        updateColorTheme(sp);  //update color theme based on settings

        //Retrieve the users Age, Height, Weight, sex from Shared Preferences.
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

        //this formula for getting calories burned per step was worked out using data from this website
        //https://www.verywellfit.com/pedometer-steps-to-calories-converter-3882595#:~:text=When%20calculating%20your%20calories%20burned,of%200.04%20calories%20per%20step.
        caloriesPerStep = weight * 0.00059756;

        calorieToggle = sp.getBoolean("calorieCount", false);
        musicToggle = sp.getBoolean("musicSync", false);

        if (musicToggle) {   //control music controls if on or off
            musicButton.setVisibility(View.VISIBLE);
        } else {
            musicButton.setVisibility(View.INVISIBLE);
        }

        steps = 0;
        calories = 0;
        seconds = 0;
        km = 0;

        sdb = new StatsDatabase(this);    //initialize instance of database to add stats

        if (paused) {
            pauseButton.setText("START");
            pauseButton.setBackgroundColor(Color.parseColor(accent));
        }

        updateStats();  //update run stats

        if (!goalInput.equals("")) {   //if user coming here from notification (already have run goal set)
            runProgressBar.setMax(Integer.parseInt(goalInput)); //set progress bar
            runProgressLabel.setText("0 / " + goalInput + " km");
        } else {
            goalInputDialog();  //get run km goal from dialog box
        }
    }


    //acquire late
    @Override
    protected void onResume() {
        super.onResume();
        //get instance of sensormanager to access sensor data with
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //check if acceleration sensor is available on device before initializing
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);   //addition
        }

        if (bound) {    //if bound to service get data that as been updating while app closed and unbind service
            //get updated data from service
            seconds = dataService.getSeconds();
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;
            // Format the seconds into hours, minutes, and seconds.
            String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
            //set the textview text
            timeElapsedTextView.setText(time);

            steps = dataService.getSteps();
            stepsTextView.setText(Integer.toString(steps));

            DecimalFormat df = new DecimalFormat("###.##");   //round to 2 decimal places

            km = dataService.getDistance();
            String kmFormatted = df.format(km);
            kmTextView.setText(kmFormatted);

            if (calorieToggle) {    //if calories is on get calories data
                calories = dataService.getCalories();
                String calorieFormatted = df.format(calories);
                caloriesTextView.setText(calorieFormatted);
            }

            if (locationToggle) {   //if location is on get location data
                //get arraylist of latlng route coordinates collected by service and add to run route arraylist
                List<LatLng> serviceLatLngList = dataService.getRunRoute();
                for (int i = 0; i < serviceLatLngList.size(); i++) {
                    LatLng currentLatLng = serviceLatLngList.get(i);
                    latLngList.add(currentLatLng);
                }
            }

            unbindService(connection);
            bound = false;
        }
    }

    //release early
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
//        paused = true;  //pause stopwatch
    }

    @Override
    protected void onStop() {
        super.onStop();

        //not sure why paused is true when it should be false??
        if (!finished && !paused) { //if user hasn't indicated they are finised (from pressing finish or exit) start service to continue counting stats while app closed
            //start service to keep gathering run data even if phone not on/app not open (during run), put in data to add to
            Intent serviceIntent = new Intent(this, CurrentRunDataService.class);
            serviceIntent.putExtra("seconds", seconds);
            serviceIntent.putExtra("steps", steps);
            serviceIntent.putExtra("km", km);
            serviceIntent.putExtra("calories", calories);
            serviceIntent.putExtra("averageStrideLength", averageStrideLength);
            serviceIntent.putExtra("caloriesPerStep", caloriesPerStep);
            serviceIntent.putExtra("calorieToggle", calorieToggle);
            serviceIntent.putExtra("locationToggle", locationToggle);

            if (locationToggle) {   //if location is on send location data
                if (latLngList.size() > 0) { //if latlng list has coordinates in it
                    int lastIndex = latLngList.size() - 1;  //get location of last recorded location
                    serviceIntent.putExtra("lastLocation", latLngList.get(lastIndex));  //send latlng of last location to service to pick up recording route from

//                    Toast.makeText(this, "sending last location to service", Toast.LENGTH_SHORT).show();
                } else {
                    //do not send a last location because there isn't one saved
//                    Toast.makeText(this, "no saved location to send to service", Toast.LENGTH_SHORT).show();
                }
            }

            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);   //bind to service (allows to get data back)
        } else {
//            Toast.makeText(this, "not starting service f p" + finished + paused, Toast.LENGTH_SHORT).show();
        }
    }

    //track running stats here
    private void updateStats() {
        //update time
        statsUpdater();

        //update calories burned
        if (!calorieToggle) {    //if calories turned off do not display calories burned stat
            caloriesTextView.setVisibility(View.GONE);
            caloriesLabel.setVisibility(View.GONE);
        } else {
            caloriesTextView.setVisibility(View.VISIBLE);
            caloriesLabel.setVisibility(View.VISIBLE);
        }
    }

    //runs stopwatch and updates seconds, updates km and calories every 3 seconds
    private void statsUpdater() {

        // Creates a new Handler
        final Handler handler = new Handler();

        // Call the post() method, passing in a new Runnable.
        // The post() method processes code without a delay, so the code in the Runnable will run almost immediately.
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes, and seconds.
                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);

                // Set the text view text.
                timeElapsedTextView.setText(time);

                //if run is in progress increment seconds
                if (!paused) {
                    seconds++;  //increment seconds

                    if (seconds % 3 == 0) {    //update calorie calculation and km progress every 3 seconds
                        DecimalFormat df = new DecimalFormat("###.##");   //round to 2 decimal places

                        if (calorieToggle) { //if calorie count is on update calories burned
                            calories = steps * caloriesPerStep;
                            String calorieFormatted = df.format(calories);
                            caloriesTextView.setText(calorieFormatted);    //update calories textview
                        }

                        //update km progress
                        double distFromSteps = calculateDistanceFromSteps(steps); //The distance is calculated in metres
                        km = distFromSteps / 1000; //Convert from m to km by dividing the value by 1000
                        String kmFormatted = df.format(km);
                        kmTextView.setText(kmFormatted);    //update km textview

                        if (goalInput.equals("")) {  //if user input a goal update progress bar
                            runProgressBar.setProgress((int) km);
                            runProgressLabel.setText(kmFormatted + " / " + goalInput + " km");
                        }
                    }
                }
                //post code again with a delay of 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

    //save data from run into SQLite database
    private void saveRunData() {
        //save run stats
        DecimalFormat df = new DecimalFormat("###.##");   //round km to 2 decimal places
        String kmFormatted = df.format(km);
        double kmDouble = Double.parseDouble(kmFormatted);

        String caloriesFormatted = df.format(calories);
        double caloriesDouble = Double.parseDouble(caloriesFormatted);

        double finalPaceDouble; //calculate pace (if enough data)backup march 10
        if (seconds != 0 && km >= 1) { //make sure not dividing by 0 and km large enough to have enough data to calculate

            double hrs = seconds / 3600;
            double paceDouble = km / hrs;
            String paceFormatted = df.format(paceDouble);   //round to 2 decimal places
            finalPaceDouble = Double.parseDouble(paceFormatted);
        } else {   //not enough data to calculate
            finalPaceDouble = 0;
        }

        long id = sdb.insertRunData(seconds, kmDouble, caloriesDouble, steps, finalPaceDouble);   //save run data
        if (id < 0) {
//            Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
        }

        if (locationToggle){    //if location on save route
//            Toast.makeText(this, "saving map table (permission)", Toast.LENGTH_SHORT).show();
            String mapTableName = sdb.addMaptable();

            //these are to show that route save polyline is working (if testing while in same place)
            latLngList.add(new LatLng(49.21734520186808, -122.67771428458336));
            latLngList.add(new LatLng(49.217140710952165, -122.68879673871635));

            //inserting longs and lats into table
            for (int i = 0; i < latLngList.size(); i++) {
                String longitude = Double.toString(latLngList.get(i).longitude);
                String latitude = Double.toString(latLngList.get(i).latitude);
                long mapId = sdb.insertMapData(longitude, latitude, mapTableName);

                if (mapId < 0) {
//                Toast.makeText(this, "map fail", Toast.LENGTH_SHORT).show();
                } else {
//                Toast.makeText(this, "map success", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())   //check what button is pressed
        {

            case R.id.pauseButton:
                //pause time elapsed and stop data collection
                if (paused) {
                    pauseButton.setText("PAUSE");
                    pauseButton.setBackgroundColor(Color.parseColor(accentLight));
                    paused = false;
                } else if (!paused) {
                    pauseButton.setText("START");
                    pauseButton.setBackgroundColor(Color.parseColor(accent));
                    paused = true;
                }
                break;

            case R.id.finishButton: //go to congrats screen post run
                if (seconds > 0) {
                    finished = true;
                    saveRunData();
                    gotoPostRun();
                } else {
                    gotoRun();  //if user didn't start run don't save anything
                }
                break;

            case R.id.exitButton: //display dialog box to confirm exiting without saving
                confirmClose(v);
                break;

            case R.id.musicButton: //go to music player when click music button
                gotoMusic();
                break;
        }
    }

    //dialog box to confirm if user wants to close run and lose data
    public void confirmClose(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);  //create alert with custom styling

        Context c = v.getContext();

        builder.setTitle("Cancel run?");
        builder.setMessage("All run data will be lost");

        //user can click away to close box
        builder.setCancelable(true);

        //if user presses yes cancel run without saving
        builder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        Toast.makeText(c, "canceling run", Toast.LENGTH_SHORT).show();
                        finished = true;
                        Intent i = new Intent(c, RunActivity.class);    //go back to run activity without saving
                        c.startActivity(i);
                        dialog.cancel();
                    }
                });

        //close window and continue current run activity
        builder.setNegativeButton(
                "No",
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


    private void gotoRun() {
        Intent intent = new Intent(this, RunActivity.class);
        startActivity(intent);
    }

    //go to music player
    private void gotoMusic() {
        Uri musicWebpage = Uri.parse("https://music.youtube.com");
        Intent musicIntent = new Intent(Intent.ACTION_VIEW, musicWebpage); //opens default browser

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(musicIntent, 0);
        boolean safe = activities.size() > 0;
        if (safe) {
            startActivity(musicIntent);
        }
    }


    private void gotoPostRun() {
        Intent intent = new Intent(this, PostRunActivity.class);
        //pass final data in bundle to post run activity
        intent.putExtra("steps", steps);

        DecimalFormat df = new DecimalFormat("###.##");   //round km to 2 decimal places
        String kmFormatted = df.format(km);
        double kmDouble = Double.parseDouble(kmFormatted);
        intent.putExtra("km", kmDouble);

        String caloriesFormatted = df.format(calories);
        double caloriesDouble = Double.parseDouble(caloriesFormatted);
        intent.putExtra("calories", caloriesDouble);

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        // format seconds into hours, minutes, seconds
        String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
        intent.putExtra("time", time);

        double finalPaceDouble;
        if (seconds != 0 && km >= 1) { //make sure not dividing by 0 and km large enough to have enough data to calculate

            double hrs = seconds / 3600;
            double paceDouble = km / hrs;
            String paceFormatted = df.format(paceDouble);   //round to 2 decimal places
            finalPaceDouble = Double.parseDouble(paceFormatted);
        } else {   //not enough data to calculate
            finalPaceDouble = 0;
        }
        intent.putExtra("pace", finalPaceDouble);
        startActivity(intent);
    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR); //get color theme preference
        accentLight = sharedPrefs.getString("themeColorLight", DEFAULT_LIGHT); //get color theme preference

        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        timeElapsedTextView.setTextColor(Color.parseColor(accent));
        caloriesTextView.setTextColor(Color.parseColor(accent));
        stepsTextView.setTextColor(Color.parseColor(accent));
        kmTextView.setTextColor(Color.parseColor(accent));

        finishButton.setBackgroundColor(Color.parseColor(accent));
    }


    //calculation of km distance travelled using step count
    public double calculateDistanceFromSteps(int stepCount) {
        if (!paused) {
            double strideLength = averageStrideLength * (1 + strideLengthVariation);
            double distance = stepCount * strideLength;
            Log.e("Dist:", String.valueOf(distance));
            return distance;
        }
        return -1;
    }

    //calculate steps and add to step total
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Check if there is a step detector sensor.
        //update sensor values
        // check if the sensor event is from the accelerometer
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (!paused) {
                // get the x, y, and z acceleration values
                float xAccel = abs(event.values[0]);
                float yAccel = abs(event.values[1]);
                float zAccel = abs(event.values[2]);

                // calculate the acceleration magnitude.
                float accelMag = (float) Math.sqrt(xAccel * xAccel + yAccel * yAccel + zAccel * zAccel);
                float accelMagDelta = accelMag - previousAccelMag;
                previousAccelMag = accelMag;

                // check if the acceleration magnitude value exceeds the threshold and update steps if so
                if (accelMagDelta > stepThreshold) {
                    steps++;
                    stepsTextView.setText(String.valueOf(steps));
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //callback that record coordinates as location changes
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (locationToggle) {    //if location is on (this should be redundant)
//            Toast.makeText(this, "location changed", Toast.LENGTH_SHORT).show();
            //Get the longitude and latitude and store it in two double variables but only when the activity is not paused.
            if (!paused) {    //update when detects a location change
                //get current latitude and longitude
                LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                latLngList.add(newLatLng);
//                Toast.makeText(this, "location changed, latlng 0: " + latLngList.get(0), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void goalInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Set Run Goal");
        builder.setMessage("Set a run goal in kilometers to motivate you for this run!");

        // Set up the input
        EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goalInput = input.getText().toString();
                if (!goalInput.equals("")) { //if input isn't null
                    runProgressBar.setMax(Integer.parseInt(goalInput)); //if user input a goal set progress bar
                    runProgressLabel.setText("0 / " + goalInput + " km");
                }
            }
        });

        builder.setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                goalInput = "";
                progressBarCard.setTransitionVisibility(View.GONE); //if no goal hide progress bar
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(accent));
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(accent));
    }


    //defines service binding callbacks and updates bound boolean
    //this source was used as a reference to learn method of creating bound services with methods/data accessible to client activity
    //https://developer.android.com/guide/components/bound-services
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {  //when bound to service
            CurrentRunDataService.CurrentRunDataBinder binder = (CurrentRunDataService.CurrentRunDataBinder) service;   //initialize bind service inner class (so we can access methods in the activity)
            dataService = binder.getService();  //initialize dataservice variable for use getting data
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) { //when service diconnected
            bound = false;
        }
    };

    //get current location of user to start run route
    private void getCurrentLocation(){
        //add current location

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "no permission granted", Toast.LENGTH_SHORT).show();
            return;
        }

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {

            @Override
            public void onSuccess(Location location) {
                if (location != null) { //check if location is null
//                    Toast.makeText(CurrentRunActivity.this, "permissions granted", Toast.LENGTH_SHORT).show();

                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    latLngList.add(currentLocation);
//                    Toast.makeText(CurrentRunActivity.this, "added current location", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(CurrentRunActivity.this, "no past location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //tell user we need location permission, give another chance to turn it on
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
                builder.setTitle("Enable location access");
                builder.setMessage("Please enable location acces to allow route tracking and local temperature information retrieval");

                // Set up the buttons
                builder.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(CurrentRunActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                });

            } else {    // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {// If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  // permission was granted, do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                         locationManager.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 400, 1, this);    //request location updates (does this work?)
                    }
                } else {
                    // permission denied - Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    //thread to get current location
    private class CurrentLocationThread implements Runnable
    {
        @Override
        public void run() {
            getCurrentLocation();
        }
    }

    //method to start get current location thread
    public void startCurrentLocationThread (){
        Thread myThread = new Thread(new CurrentLocationThread());
        myThread.start();
    }



}