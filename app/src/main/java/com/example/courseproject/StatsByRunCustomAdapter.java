package com.example.courseproject;

//adapter for taking stats data from sqlite and putting it in recyclerview

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatsByRunCustomAdapter extends RecyclerView.Adapter<StatsByRunCustomAdapter.MyViewHolder> {

    public ArrayList<String> list;
    Context context;

    public StatsByRunCustomAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public StatsByRunCustomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_by_run_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);

        context = v.getContext();   //for toasts

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StatsByRunCustomAdapter.MyViewHolder holder, int position) {

        String[] results = (list.get(position).toString()).split(","); //split results using ,

        SharedPreferences sp = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        boolean calorieToggle = sp.getBoolean("calorieCount", false);      //get if calorie calculator is turned on or off

        //write date in easier form for human reading
        try {
            Date runDate = new SimpleDateFormat("dd-MM-yyyy").parse(results[0]);

            SimpleDateFormat dateWords = new SimpleDateFormat("dd MMMM yyyy");
            String dateWordsString = (dateWords.format(runDate));

            holder.dateTextView.setText(dateWordsString); //date

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //show - instead of 0 for non-initialized stats
        if (Integer.parseInt(results[2]) == 0) { //steps
            holder.stepsTextView.setText("-");   //uninitialized
        } else {
            holder.stepsTextView.setText(results[2]);
        }

        if (Double.parseDouble(results[3]) == 0) { //km (changed to double)
            holder.kmTextView.setText("-");   //uninitialized
        } else {
            holder.kmTextView.setText(results[3]);
        }

        if (Double.parseDouble(results[4]) == 0) { //calories (changed to double)
            holder.caloriesTextView.setText("-");   //uninitialized
        } else {
            holder.caloriesTextView.setText(results[4]);
        }

        if (Double.parseDouble(results[5]) == 0) { //pace (changed to double)
            holder.paceTextView.setText("-");   //uninitialized
        } else {
            holder.paceTextView.setText(results[5] + " km/hr");
        }

        if (Integer.parseInt(results[1]) == 0) { //activity time
            holder.timeTextView.setText("-");   //uninitialized
        } else {
            //format activity time into hours, mins, seconds
            int seconds = Integer.parseInt(results[1]);
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;
            String formattedTime = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
            holder.timeTextView.setText(formattedTime);
        }

        if (!calorieToggle) {    //if calories turned off do not display calories burned stat
            holder.caloriesTextView.setVisibility(View.GONE);
            holder.caloriesLabel.setVisibility(View.GONE);
        } else {
            holder.caloriesTextView.setVisibility(View.VISIBLE);
            holder.caloriesLabel.setVisibility(View.VISIBLE);
        }

        holder.UID = results[6];
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView dateTextView, stepsTextView, kmTextView, timeTextView, caloriesTextView, caloriesLabel, paceTextView;

        public LinearLayout myLayout;

        //for accessing SQLite database
        private StatsDatabase sdb;
        private StatsDatabaseHelper statsDatabaseHelper;
        private Cursor cursor;

        public String UID;  //for matching map table with stats database
        List<LatLng> latLngArrayList;

        Context c;

        String accent;  //for colour theme
        public static final String DEFAULT_COLOR = "#FF4081";

        public MyViewHolder(View itemView) {
            super(itemView);
            myLayout = (LinearLayout) itemView;

            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            stepsTextView = (TextView) itemView.findViewById(R.id.stepsTextView);
            kmTextView = (TextView) itemView.findViewById(R.id.kmTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            caloriesTextView = (TextView) itemView.findViewById(R.id.caloriesTextView);
            caloriesLabel = (TextView) itemView.findViewById(R.id.caloriesLabel);
            paceTextView = (TextView) itemView.findViewById(R.id.paceTextView);

            itemView.setOnClickListener(this);  //set as on click listener
            c = itemView.getContext();

            updateColorTheme();

        }

        private void updateColorTheme() {
            //get color theme preference
            SharedPreferences sp = c.getSharedPreferences("UserData", Context.MODE_PRIVATE);
            accent = sp.getString("themeColor", DEFAULT_COLOR);
            //set text color to accent color
            stepsTextView.setTextColor(Color.parseColor(accent));
            kmTextView.setTextColor(Color.parseColor(accent));
            timeTextView.setTextColor(Color.parseColor(accent));
            caloriesTextView.setTextColor(Color.parseColor(accent));
            paceTextView.setTextColor(Color.parseColor(accent));
        }


        @Override
        public void onClick(View view) {
            //go to specified run route activity when clicked
            try{    //if there is saved run route go to route activity
                if (mapRouteSaved()){
                    Intent mapsIntent = new Intent(c, RouteActivity.class);
                    mapsIntent.putExtra("TABLE_NAME",UID);
                    c.startActivity(mapsIntent);
                }
                else{   //if not show error dialog and don't crash
                    errorDialog("No route saved", "This run does not have a saved route");
                }
            }
            catch(Exception mapsError){
                errorDialog("No route saved", "This run does not have a saved route");
            }

        }

        private boolean mapRouteSaved() {
            sdb = new StatsDatabase(c);
            statsDatabaseHelper = new StatsDatabaseHelper(c);    //creating database

            cursor = sdb.getMapData(UID);

            if (cursor != null){    //if there is a run route table
                return true;
                }
            return false;
            }


        // make dialog come up when no input added
        public void errorDialog(String title, String message) {

            AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
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
    }

}
