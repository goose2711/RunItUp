package com.example.courseproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RunSchedulingCalendarActivity extends AppCompatActivity implements View.OnClickListener, CalendarAdapter.MyClickListener {

    private TextView monthYearTextView, titleTextView;
    private LocalDate selectedDate;

    private RecyclerView calendarRecycler;
    private RecyclerView.Adapter calendarAdapter;
    private RecyclerView.LayoutManager calendarLayoutManager;

    private ImageButton previousMonthButton, nextMonthButton, exitButton;

    private String inputType;   //for saving if past or future run is being input

    String accent;  //for colour theme
    public static final String DEFAULT_COLOR = "#FF4081";

    String previousActivity;    //for going back to correct activity if exited


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_scheduling_calendar);

        previousMonthButton = (ImageButton) findViewById(R.id.previousMonthButton);
        previousMonthButton.setOnClickListener(this);

        nextMonthButton = (ImageButton) findViewById(R.id.nextMonthButton);
        nextMonthButton.setOnClickListener(this);

        exitButton = (ImageButton) findViewById(R.id.exitButton);
        exitButton.setOnClickListener(this);

        calendarRecycler = findViewById(R.id.calendarRecyclerView);
        monthYearTextView = findViewById(R.id.monthTextView);

        titleTextView = (TextView) findViewById(R.id.titleTextView);

        previousActivity = getIntent().getExtras().getString("prevActivity");   //get activity that started this activity

        SharedPreferences sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        updateColorTheme(sharedPrefs);  //update color theme based on settings

        selectedDate = LocalDate.now(); //get current date in YYYY-MM-DD

        setUpRecycler(selectedDate);

        inputType = getIntent().getExtras().getString("input_type");
        if (inputType.equals("past")) {  //if user inputing past run retroactively
            titleTextView.setText("Input Past Run");
        }
        if (inputType.equals("future")) {  //if user scheduling future run
            titleTextView.setText("Schedule Future Run");
        }
    }

    private void setUpRecycler(LocalDate selectedDate) {
        monthYearTextView.setText(monthYearFromDate(selectedDate)); //get month and year from date to set calendar title
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate); //get array of days in selected month

        calendarLayoutManager = new GridLayoutManager(this, 7); //new grid layout recycler to show weeks in month calendar
        calendarRecycler.setLayoutManager(calendarLayoutManager);

        calendarAdapter = new CalendarAdapter(daysInMonth, this); //initialize calendar recyclerview with days array
        calendarRecycler.setAdapter(calendarAdapter);
    }


    //make array of days in month from date parameter
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for (int i = 1; i <= 42; i++) {
            if (i <= dayOfWeek || i > daysInMonth + dayOfWeek) {
                daysInMonthArray.add("");
            } else {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }
        return daysInMonthArray;
    }

    //get month and year from date parameter
    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction() {
        selectedDate = selectedDate.minusMonths(1);
        setUpRecycler(selectedDate);
    }

    public void nextMonthAction() {
        selectedDate = selectedDate.plusMonths(1);
        setUpRecycler(selectedDate);
    }

    private void gotoAddRun() {
        Intent i = new Intent(this, AddRunActivity.class);
        startActivity(i);
    }

    private void gotoRunDetails(String runDate, String inputType) {
        Intent i = new Intent(this, InputRunActivity.class);
        i.putExtra("run_date", runDate); //adding run date
        i.putExtra("input_type", inputType); //adding run input type (past or future)
        i.putExtra("prevActivity", previousActivity); //what activity started the intent/activity
        startActivity(i);
    }

    public void gotoFutureRunSchedule() {
        Intent i = new Intent(this, FutureRunScheduleActivity.class);
        startActivity(i);
    }

    public void gotoRun() {
        Intent i = new Intent(this, RunActivity.class);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextMonthButton:
                nextMonthAction();  //go to next month calendar
                break;

            case R.id.previousMonthButton:
                previousMonthAction();  //go to past month calendar
                break;

            case R.id.exitButton:
                switch (previousActivity) {
                    case "future_run_schedule_activity":    //if came from run scheduling calendar return there
                        gotoFutureRunSchedule();
                        break;

                    case "add_run_activity":    //from run log add run page
                        gotoAddRun();  //if came from add run activity return there
                        break;

                    case "run_activity":    //from post run schedule button
                        gotoRun();  //if came from add run activity return there
                        break;
                }
                break;
        }
    }

    @Override
    public void myOnClick(int position, String dayText) {
        if (!dayText.equals("")) {

            LocalDate today = LocalDate.now();  //get today's date

            int year = selectedDate.getYear();
            int month = selectedDate.getMonthValue();
            int day = Integer.parseInt(dayText);

            String date = Integer.toString(day) + "-" + Integer.toString(month) + "-" + Integer.toString(year);

            if (date.charAt(1) == '-') date = "0" + date;
            if (date.charAt(4) == '-') date = date.substring(0, 3) + "0" + date.substring(3);

            LocalDate runInputDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));    //selected date

            if (inputType.equals("past")) { //if inputting past run
                if (runInputDate.isBefore(today)) {
                    gotoRunDetails(date, inputType);
                } else {
                    errorDialog("Date selected not in past", "Please select a past date to input a run retroactively.");
                }
            } else if (inputType.equals("future")) {    //if scheduling future run
                if (runInputDate.isAfter(today)) {
                    gotoRunDetails(date, inputType);
                } else {
                    errorDialog("Date selected not in future", "Please select a future date to schedule a new run.");
                }
            }
        }
    }


    //check and update color theme based on sharedpreferences
    private void updateColorTheme(SharedPreferences sharedPrefs) {
        //get color theme preference
        accent = sharedPrefs.getString("themeColor", DEFAULT_COLOR);
        titleTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color

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
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(accent));
    }


}