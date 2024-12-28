package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.List;

//activity for showing saved run route
public class RouteActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap map;

    private String mapUID;

    //for database access
    private StatsDatabase sdb;
    private StatsDatabaseHelper statsDatabaseHelper;
    Cursor cursor;

    List<LatLng> latLngArrayList;

    PolylineOptions polylineOptions = new PolylineOptions();

    ImageButton exitButton;
    TextView titleTextView;

    public static final String DEFAULT_COLOR = "#FF4081";
    String accent;  //for colour theme


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.routeMap);
        mapFragment.getMapAsync(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton); //exit button to close without saving
        exitButton.setOnClickListener(this);
        titleTextView = (TextView) findViewById(R.id.titleTextView);  //for color change with theme

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sharedPrefs);  //update color theme based on settings

        if (getIntent().hasExtra("TABLE_NAME")) {
            mapUID = getIntent().getStringExtra("TABLE_NAME");
        }

        getMapValues(); //get lat and lng doubles from map route table
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        //googleMap.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);

        polylineOptions.color(Color.parseColor(accent));
        polylineOptions.width(10);

        if (latLngArrayList.size() > 0) {
            polylineOptions.addAll(latLngArrayList);
            googleMap.addPolyline(polylineOptions);

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLngArrayList.get(0), 15);
            map.moveCamera(update);
        }
    }

    private void getMapValues() {
        sdb = new StatsDatabase(this);
        statsDatabaseHelper = new StatsDatabaseHelper(this);    //creating database

        cursor = sdb.getMapData(mapUID);

        if (cursor != null) {    //if there is a run route table
            //get data for route polyline arraylist
            int latIndex = cursor.getColumnIndex(StatsConstants.LAT);
            int longIndex = cursor.getColumnIndex(StatsConstants.LNG);

            latLngArrayList = new ArrayList<LatLng>();
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add lat and lng doubles to latlng objects in polyline arraylist
                Double lat = cursor.getDouble(latIndex);
                Double lng = cursor.getDouble(longIndex);

                LatLng latlng = new LatLng(lat, lng);
                latLngArrayList.add(latlng);
                cursor.moveToNext();    //go to next row (if there is one)
            }
        }
    }

    @Override
    public void onClick(View v) {
        gotoRun();  //return to run activity
    }

    public void gotoRun() {
        Intent runIntent = new Intent(this, RunActivity.class);
        startActivity(runIntent);
    }

    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
    }
}