package com.example.courseproject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecoveryCustomAdapter extends RecyclerView.Adapter<RecoveryCustomAdapter.ViewHolder> {

    private RecyclerData recoveryData;

    private static Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView recoveryTitle, recoveryContent, recoveryLink;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            recoveryTitle = (TextView) view.findViewById(R.id.nextRunTitle);
            recoveryContent = (TextView) view.findViewById(R.id.nextRunDateTextView);
            recoveryLink = (TextView) view.findViewById(R.id.nextRunTimeTextView);
        }

        public TextView getTextView() {
            return recoveryTitle;
        }

        //implicit intent for different links on click
        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();    //get position of recyclerview pressed


            if (position == 0) { //go stretching intent
                Uri stretchingWebpage = Uri.parse("https://www.ymcahome.ca/stretch-and-core");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, stretchingWebpage); //opens default browser
                //check device can execute intent
                PackageManager packageManager = v.getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
                boolean safe = activities.size() > 0;
                if(safe) {
                    v.getContext().startActivity(webIntent);
                }
            }
            if (position == 1) {
                Uri limitsWebpage = Uri.parse("https://csepguidelines.ca/");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, limitsWebpage); //opens default browser
                //check device can execute intent
                PackageManager packageManager = v.getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
                boolean safe = activities.size() > 0;
                if(safe) {
                    v.getContext().startActivity(webIntent);
                }
            }
            if (position == 2) {
                Uri nutritionWebpage = Uri.parse("https://food-guide.canada.ca/en/");
                Intent webIntent = new Intent(Intent.ACTION_VIEW, nutritionWebpage); //opens default browser
//                //check device can execute intent
                PackageManager packageManager = v.getContext().getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(webIntent, 0);
                boolean safe = activities.size() > 0;
                if(safe) {
                    v.getContext().startActivity(webIntent);
                }
            }

        }
    }

    //initialize adapter dataset
    public RecoveryCustomAdapter(RecyclerData recoveryContent) {
        recoveryData = recoveryContent;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recovery_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    //associate viewholder with data
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from data at this position and replace the
        // contents of the view with that element
        viewHolder.recoveryTitle.setText(recoveryData.recovery_titles[position]);
        viewHolder.recoveryContent.setText(recoveryData.recovery_content[position]);
        viewHolder.recoveryLink.setText(recoveryData.recovery_links[position]);
    }

    //return size of dataset
    @Override
    public int getItemCount() {
        return recoveryData.recovery_titles.length;
    }


}
