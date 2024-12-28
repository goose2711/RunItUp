package com.example.courseproject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class JournalCustomAdapter extends RecyclerView.Adapter<JournalCustomAdapter.MyViewHolder> {

    public ArrayList<String> list;
    Context context;

    public JournalCustomAdapter(ArrayList<String> list) {
        this.list = list;
    }

    @Override
    public JournalCustomAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_layout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(v);

        context = v.getContext();   //for toasts

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(JournalCustomAdapter.MyViewHolder holder, int position) {

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

        holder.entryTextView.setText(results[1]);

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView dateTextView;
        public TextView entryTextView;

        public LinearLayout myLayout;

        private String accent; //for colour theme
        public static final String DEFAULT_COLOR = "#FF4081";

        //for accessing SQLite database
        private JournalDatabase jdb;
        private JournalDatabaseHelper dataHelper;
        private Cursor cursor;

        Context c;

        public MyViewHolder(View itemView) {
            super(itemView);
            myLayout = (LinearLayout) itemView;

            dateTextView = (TextView) itemView.findViewById(R.id.entryDate);
            entryTextView = (TextView) itemView.findViewById(R.id.entryContent);

            itemView.setOnClickListener(this);
            c = itemView.getContext();
            updateColorTheme(c);
        }

        // make delete dialog come up when press on entry
        public void deleteEntryDialog() {
            // Create the object of AlertDialog Builder class
            AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);

            // Set Alert Title
            builder.setTitle("Delete entry?");

            //user can click away to close box
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            Toast.makeText(c, "deleting entry ", Toast.LENGTH_SHORT).show();

                            //find selected entry and delete it
                            findSelectedEntry(entryTextView.getText().toString());   //this actually works wow!!!!!!!!!!! :D

                            Intent i = new Intent(c, JournalActivity.class);    //need to refresh journal activity to show the entry is deleted (how else can we do this?)
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
        public void deleteEntry(String entry) {
            dataHelper = new JournalDatabaseHelper(c);
            SQLiteDatabase db = dataHelper.getWritableDatabase();
            db.execSQL(" DELETE FROM " + JournalConstants.TABLE_NAME + " WHERE " + JournalConstants.ENTRY + "=\"" + entry + "\";");
        }

        //find entry in table by entry content of entry selected
        private void findSelectedEntry(String selectedEntry) {
            jdb = new JournalDatabase(c);
            cursor = jdb.getData();

            //get data for recyclerview
            int index2 = cursor.getColumnIndex(JournalConstants.ENTRY);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) { //while there are still unread rows in table go through and add them to comma separated String
                String entry = cursor.getString(index2);
                if (entry.equals(selectedEntry)) {
                    deleteEntry(entry);
//                    Toast.makeText(c, "found entry to delete", Toast.LENGTH_SHORT).show();
                    break;
                }
                cursor.moveToNext();    //go to next row (if there is one)
            }
        }


        @Override
        public void onClick(View view) {
            deleteEntryDialog();    //make delete dialog box appear
        }

        //check and update color theme based on sharedpreferences
        private void updateColorTheme(Context c) {
            //get color theme preference
            SharedPreferences sp = c.getSharedPreferences("UserData", Context.MODE_PRIVATE);
            accent = sp.getString("themeColor", DEFAULT_COLOR);
        }

    }


}
