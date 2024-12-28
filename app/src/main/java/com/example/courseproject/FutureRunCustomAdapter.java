package com.example.courseproject;


//adapter for taking future run dates and putting in recyclerview

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FutureRunCustomAdapter extends RecyclerView.Adapter<FutureRunCustomAdapter.MyViewHolder> {

    public ArrayList<String> list;
    Context context;

    public FutureRunCustomAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public FutureRunCustomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.run_schedule_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);

        context = v.getContext();   //for toasts

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(FutureRunCustomAdapter.MyViewHolder holder, int position) {

        String[] results = (list.get(position).toString()).split(","); //split results using ,

        //write date in easier form for human reading
        try {
            Date runDate = new SimpleDateFormat("dd-MM-yyyy").parse(results[0]);

            SimpleDateFormat dateWords = new SimpleDateFormat("dd MMMM yyyy");
            String dateWordsString = (dateWords.format(runDate));

            holder.dateTextView.setText(dateWordsString); //date

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.timeTextView.setText(results[1]);
        holder.kmTextView.setText(results[2] + " km");

        holder.messageTextView.setText(results[3]);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView dateTextView, timeTextView, kmTextView, messageTextView;

        public LinearLayout myLayout;

        //for accessing SQLite database
        private RunDatabase rdb;
        private RunDatabaseHelper dataHelper;
        private Cursor cursor;

        String accent;  //for colour theme
        public static final String DEFAULT_COLOR = "#FF4081";

        Context c;

        public MyViewHolder(View itemView) {
            super(itemView);
            myLayout = (LinearLayout) itemView;

            dateTextView = (TextView) itemView.findViewById(R.id.dateTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            kmTextView = (TextView) itemView.findViewById(R.id.kmTextView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);

            itemView.setOnClickListener(this);  //set as on click listener
            c = itemView.getContext();

            updateColorTheme();
        }

        private void updateColorTheme() {
            //get color theme preference
            SharedPreferences sp = c.getSharedPreferences("UserData", Context.MODE_PRIVATE);
            accent = sp.getString("themeColor", DEFAULT_COLOR);
            dateTextView.setTextColor(Color.parseColor(accent));  //set text color to accent color
        }

        // make delete dialog come up when press on entry
        public void deleteEntryDialog() {
            // Create the object of AlertDialog Builder class
            AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);

            // Set Alert Title
            builder.setTitle("Delete run?");

            //user can click away to close box
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            Toast.makeText(c, "deleting entry", Toast.LENGTH_SHORT).show();

                            //find selected entry and delete it
                            findSelectedEntry(messageTextView.getText().toString());   //this actually works wow!!!!!!!!!!! :D

                            Intent i = new Intent(c, FutureRunScheduleActivity.class);    //need to refresh future run activity to show the entry is deleted (how else can we do this?)
                            c.startActivity(i);

                            dialog.cancel();
                        }
                    });

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

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(accent));
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(accent));
        }

        //delete entry by accessing its content
        public void deleteEntry(String msg) {
            dataHelper = new RunDatabaseHelper(c);
            SQLiteDatabase db = dataHelper.getWritableDatabase();
            db.execSQL(" DELETE FROM " + RunConstants.TABLE_NAME + " WHERE " + RunConstants.MESSAGE + "=\"" + msg + "\";");
        }

        //find entry in table by entry content of entry selected
        private void findSelectedEntry(String selectedMsg) {
            rdb = new RunDatabase(c);
            cursor = rdb.getData();

            //get data for recyclerview
            int runMsgIndex = cursor.getColumnIndex(RunConstants.MESSAGE);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
                String runMsg = cursor.getString(runMsgIndex);
                if (runMsg.equals(selectedMsg)) {
                    deleteEntry(runMsg);
                    break;
                }
                cursor.moveToNext();    //go to next row (if there is one)
            }
        }


        @Override
        public void onClick(View view) {
            deleteEntryDialog();    //make delete dialog box appear

        }
    }
}

