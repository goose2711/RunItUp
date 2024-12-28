package com.example.courseproject;

import static java.lang.Math.abs;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

//service to continue collecting run data while app not open/seen
public class CurrentRunDataService extends Service implements SensorEventListener, LocationListener {

    private final IBinder binder = new CurrentRunDataBinder();

    //stats variables
    private int seconds;    //for stopwatch
    private int steps;        //variables for run stats
    private double km, calories;  //variable for km (changed from int)

    //calculations variables
    private static final int stepThreshold = 5; // adjust this value to calibrate the step detection
    private float previousAccelMag = 0;
    private double averageStrideLength;
    private static final double strideLengthVariation = 0.05; // Variation in stride length as a decimal percentage
    double caloriesPerStep;

    boolean calorieToggle, locationToggle;  //for if calorie calculation/location access are turned off or on

    SensorManager sensorManager;    //getting sensor to track running
    Sensor accelerometer;

    List<LatLng> latLngList = new ArrayList<>();

    private LatLng lastLocation;    //last location saved sent in intent


    public class CurrentRunDataBinder extends Binder {
        CurrentRunDataService getService() {
            //returns instance of service current run activity can call public methods with (and get updated stats)
            return CurrentRunDataService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
//        Toast.makeText(this, "starting service", Toast.LENGTH_SHORT).show();

        //get data from current run activity (to keep adding to)
        seconds = intent.getExtras().getInt("seconds");
        steps = intent.getExtras().getInt("steps");
        km = intent.getExtras().getDouble("km");
        calories = intent.getExtras().getDouble("calories");
        averageStrideLength = intent.getExtras().getDouble("averageStrideLength");
        caloriesPerStep = intent.getExtras().getDouble("caloriesPerStep");
        calorieToggle = intent.getExtras().getBoolean("calorieToggle");
        locationToggle = intent.getExtras().getBoolean("locationToggle");

        if (locationToggle){
            lastLocation = (LatLng)intent.getExtras().get("lastLocation");
            if (lastLocation != null){
                latLngList.add(lastLocation);   //start locations list at last location
            }
        }

        //register sensor as late as possible
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {    //check if acceleration sensor is available on device before initializing
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener((SensorEventListener) this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }

        updateStats();  //update stats

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    //methods to return updated values to current run activity (when goes back into onresume)
    public int getSeconds(){
        return seconds;
    }

    public int getSteps(){
        return steps;
    }

    public double getDistance(){
        return km;
    }

    public double getCalories(){
        return calories;
    }

    //return coordinates gathered while service was on
    public List<LatLng> getRunRoute(){
        //these are to show route saving polyline works (if testing in place)
        latLngList.add(new LatLng(49.21996611792887, -122.67001098004773));
        latLngList.add(new LatLng(49.21301407814124, -122.68297141442427));

        return latLngList;
    }

    //updates stats
    private void updateStats() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                    seconds++;  //increment seconds

                    if (seconds % 3 == 0)  {    //update calorie calculation and km progress every 3 seconds
                        if (calorieToggle){ //if calorie count is on update calories burned
                            calories = steps * caloriesPerStep;
                        }
                        //update km progress
                        double distFromSteps = calculateDistanceFromSteps(steps); //The distance is calculated in metres
                        km = distFromSteps/1000; //Convert from m to km by dividing the value by 1000
                    }
                //post code again with 1 second delay
                handler.postDelayed(this, 1000);
            }
        });
    }

    //calculation of km distance travelled using step count
    public double calculateDistanceFromSteps(int stepCount) {
            double strideLength = averageStrideLength * (1 + strideLengthVariation);
            double distance = stepCount * strideLength;
            return distance;
    }

    //calculate steps and add to step total
    @Override
    public void onSensorChanged(SensorEvent event) {
        //Check if there is a step detector sensor.
        //update sensor values
        // check if the sensor event is from the accelerometer
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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
                }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //callback that record coordinates as location changes
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (locationToggle){    //if location is on (this should be redundant)
            //Get the longitude and latitude and store it in two double variables but only when the activity is not paused.
            //get current latitude and longitude
            LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            latLngList.add(newLatLng);
//            Toast.makeText(this, "location changed, latlng 0: " + latLngList.get(0), Toast.LENGTH_SHORT).show();
        }
    }

}