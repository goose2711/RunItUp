package com.example.courseproject;

//adapter for taking stats data from sqlite and putting it in recyclerview

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class StatsByStatCustomAdapter extends RecyclerView.Adapter<StatsByStatCustomAdapter.MyViewHolder> {

    public ArrayList<String> list;
    Context context;

    public StatsByStatCustomAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public StatsByStatCustomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);

        context = v.getContext();   //for toasts

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(StatsByStatCustomAdapter.MyViewHolder holder, int position) {

        String[] results = (list.get(position).toString()).split(","); //split results using ,

        SharedPreferences sp = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
        boolean calorieToggle = sp.getBoolean("calorieCount", false);      //get if calorie calculator is turned on or off


        if (!calorieToggle && results[0].equals("CALORIES BURNED")) {    //if calories turned off hide card
            holder.card.setVisibility(View.GONE);
        }

        if (results[0].equals("ACTIVITY TIME")) {   //handle activity time display when uninitialized
            holder.statType.setText(results[0]); //stat type

            //show - instead of 0 for non-initialized stats
            if (results[1].toString().equals("0:00:00")) {
                holder.weekStat.setText("-");   //uninitialized
            } else {
                holder.weekStat.setText(results[1]);    //stat in last week
            }

            if (results[2].toString().equals("0:00:00")) {
                holder.monthStat.setText("-");   //uninitialized
            } else {
                holder.monthStat.setText(results[2]);    //stat in last month
            }

            if (results[3].toString().equals("0:00:00")) {
                holder.sixMonthStat.setText("-");   //uninitialized
            } else {
                holder.sixMonthStat.setText(results[3]);    //stat in last 6 months
            }

            if (results[4].toString().equals("0:00:00")) {
                holder.yearStat.setText("-");   //uninitialized
            } else {
                holder.yearStat.setText(results[4]);    //stat in last year
            }

            if (results[5].toString().equals("0:00:00")) {
                holder.lifetimeStat.setText("-");   //uninitialized
            } else {
                holder.lifetimeStat.setText(results[5]);    //total recorded stat data in usage lifetime
            }
        } else if (results[0].equals("AVERAGE PACE")) {   //add km/hr text to average pace
            holder.statType.setText(results[0]); //stat type

            //show - instead of 0 for non-initialized stats
            if (Double.parseDouble(results[1]) == 0) {
                holder.weekStat.setText("-");   //uninitialized
            } else {
                holder.weekStat.setText(results[1] + " km/hr");    //stat in last week
            }

            if (Double.parseDouble(results[2]) == 0) {
                holder.monthStat.setText("-");   //uninitialized
            } else {
                holder.monthStat.setText(results[2] + " km/hr");    //stat in last month
            }

            if (Double.parseDouble(results[3]) == 0) {
                holder.sixMonthStat.setText("-");   //uninitialized
            } else {
                holder.sixMonthStat.setText(results[3] + " km/hr");    //stat in last 6 months
            }

            if (Double.parseDouble(results[4]) == 0) {
                holder.yearStat.setText("-");   //uninitialized
            } else {
                holder.yearStat.setText(results[4] + " km/hr");    //stat in last year
            }

            if (Double.parseDouble(results[5]) == 0) {
                holder.lifetimeStat.setText("-");   //uninitialized
            } else {
                holder.lifetimeStat.setText(results[5] + " km/hr");    //total recorded stat data in usage lifetime
            }
        } else if (results[0].equals("KM TRAVELED") || results[0].equals("CALORIES BURNED")) {     //if stat type is km or calories parse doubles not ints
            holder.statType.setText(results[0]); //stat type

            //show - instead of 0 for non-initialized stats
            if (Double.parseDouble(results[1]) == 0) {
                holder.weekStat.setText("-");   //uninitialized
            } else {
                holder.weekStat.setText(results[1]);    //stat in last week
            }

            if (Double.parseDouble(results[2]) == 0) {
                holder.monthStat.setText("-");   //uninitialized
            } else {
                holder.monthStat.setText(results[2]);    //stat in last month
            }

            if (Double.parseDouble(results[3]) == 0) {
                holder.sixMonthStat.setText("-");   //uninitialized
            } else {
                holder.sixMonthStat.setText(results[3]);    //stat in last 6 months
            }

            if (Double.parseDouble(results[4]) == 0) {
                holder.yearStat.setText("-");   //uninitialized
            } else {
                holder.yearStat.setText(results[4]);    //stat in last year
            }

            if (Double.parseDouble(results[5]) == 0) {
                holder.lifetimeStat.setText("-");   //uninitialized
            } else {
                holder.lifetimeStat.setText(results[5]);    //total recorded stat data in usage lifetime
            }
        } else {   //if not calories card or activity time card set content as normal (handling uninitialized)
            holder.statType.setText(results[0]); //stat type

            //show - instead of 0 for non-initialized stats
            if (Integer.parseInt(results[1]) == 0) {
                holder.weekStat.setText("-");   //uninitialized
            } else {
                holder.weekStat.setText(results[1]);    //stat in last week
            }

            if (Integer.parseInt(results[2]) == 0) {
                holder.monthStat.setText("-");   //uninitialized
            } else {
                holder.monthStat.setText(results[2]);    //stat in last month
            }

            if (Integer.parseInt(results[3]) == 0) {
                holder.sixMonthStat.setText("-");   //uninitialized
            } else {
                holder.sixMonthStat.setText(results[3]);    //stat in last 6 months
            }

            if (Integer.parseInt(results[4]) == 0) {
                holder.yearStat.setText("-");   //uninitialized
            } else {
                holder.yearStat.setText(results[4]);    //stat in last year
            }

            if (Integer.parseInt(results[5]) == 0) {
                holder.lifetimeStat.setText("-");   //uninitialized
            } else {
                holder.lifetimeStat.setText(results[5]);    //total recorded stat data in usage lifetime
            }
        }
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView statType, weekStat, monthStat, sixMonthStat, yearStat, lifetimeStat;
        public LinearLayout card;    //for hiding when calories turned off

        public LinearLayout myLayout;

        Context c;

        String accent;  //for colour theme
        public static final String DEFAULT_COLOR = "#FF4081";

        public MyViewHolder(View itemView) {
            super(itemView);
            myLayout = (LinearLayout) itemView;

            statType = (TextView) itemView.findViewById(R.id.statType);
            weekStat = (TextView) itemView.findViewById(R.id.weekStat);
            monthStat = (TextView) itemView.findViewById(R.id.monthStat);
            sixMonthStat = (TextView) itemView.findViewById(R.id.sixMonthStat);
            yearStat = (TextView) itemView.findViewById(R.id.yearStat);
            lifetimeStat = (TextView) itemView.findViewById(R.id.lifetimeStat);

            card = (LinearLayout) itemView.findViewById(R.id.card);

            itemView.setOnClickListener(this);  //set as on click listener
            c = itemView.getContext();

            updateColorTheme();

        }

        private void updateColorTheme() {
            SharedPreferences sp = c.getSharedPreferences("UserData", Context.MODE_PRIVATE);    //get color theme preference
            accent = sp.getString("themeColor", DEFAULT_COLOR);

            weekStat.setTextColor(Color.parseColor(accent));    //set text color to accent color
            monthStat.setTextColor(Color.parseColor(accent));
            sixMonthStat.setTextColor(Color.parseColor(accent));
            yearStat.setTextColor(Color.parseColor(accent));
            lifetimeStat.setTextColor(Color.parseColor(accent));
        }


        @Override
        public void onClick(View view) {
        }
    }
}
