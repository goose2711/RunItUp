package com.example.courseproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {

    private final ArrayList<String> daysOfMonth;
    private Context context;
    private final MyClickListener myClickListener;

    public CalendarAdapter(ArrayList<String> daysOfMonth, MyClickListener myClickListener) {
        this.daysOfMonth = daysOfMonth;
        this.myClickListener = myClickListener;
    }

    @Override
    public CalendarAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);

        CalendarAdapter.MyViewHolder viewHolder = new CalendarAdapter.MyViewHolder(v, myClickListener);
        context = v.getContext();
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(CalendarAdapter.MyViewHolder holder, int position) {
        holder.dayOfMonth.setText(daysOfMonth.get(position));
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface MyClickListener {
        void myOnClick(int position, String dayText);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView dayOfMonth;
        private final CalendarAdapter.MyClickListener myClickListener;

        public MyViewHolder(View itemView, CalendarAdapter.MyClickListener myClickListener) {
            super(itemView);
            dayOfMonth = itemView.findViewById(R.id.dateTextView);
            this.myClickListener = myClickListener;
            itemView.setOnClickListener(this);
        }

        public void onClick(View view) {
            myClickListener.myOnClick(getAdapterPosition(), (String) dayOfMonth.getText());
        }
    }
}


