package com.example.courseproject;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class SettingsCustomAdapter extends RecyclerView.Adapter<SettingsCustomAdapter.ViewHolder>{

    private RecyclerData settingsOptions;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView settingTextView;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            settingTextView = (TextView) view.findViewById(R.id.nextRunTitle);
        }

        public TextView getTextView() {
            return settingTextView;
        }

        //go to specific activity page when clicked on
        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();    //get position of recyclerview pressed

            if (position == 0){ //update colour theme
                Intent intent = new Intent (v.getContext(), ColorThemeActivity.class);
                v.getContext().startActivity(intent);
            }
            else if(position == 1){ //update activity goal
                Intent intent = new Intent (v.getContext(), ActivityGoalActivity.class);
                v.getContext().startActivity(intent);
            }
            else if(position == 2){ //toggle calorie calculator on or off
                Intent intent = new Intent (v.getContext(), CalorieCountActivity.class);
                v.getContext().startActivity(intent);
            }
            else if(position == 3){ //change height
                Intent intent = new Intent (v.getContext(), UserDataInputActivity.class);
                intent.putExtra("input_type", "height");
                v.getContext().startActivity(intent);
            }
            else if(position == 4){ //change weight
                Intent intent = new Intent (v.getContext(), UserDataInputActivity.class);
                intent.putExtra("input_type", "weight");
                v.getContext().startActivity(intent);
            }
            else if(position == 5){ //change age
                Intent intent = new Intent (v.getContext(), UserDataInputActivity.class);
                intent.putExtra("input_type", "age");
                v.getContext().startActivity(intent);
            }
            else if(position == 6){ //change sex
                Intent intent = new Intent (v.getContext(), UserDataInputActivity.class);
                intent.putExtra("input_type", "sex");
                v.getContext().startActivity(intent);
            }
            else if(position == 7){   //change username
            Intent intent = new Intent (v.getContext(), UsernamePasswordChangeActivity.class);
            intent.putExtra("dataToChange", "username");
            v.getContext().startActivity(intent);
            }
            else if(position == 8){   //change password
                Intent intent = new Intent (v.getContext(), UsernamePasswordChangeActivity.class);
                intent.putExtra("dataToChange", "password");
                v.getContext().startActivity(intent);
            }
            else if(position == 9){ //toggle weather preference on and off
                Intent intent = new Intent (v.getContext(), WeatherPreferenceActivity.class);
                v.getContext().startActivity(intent);
            }
            else if(position == 10){ //toggle run scheduling on and off
                Intent intent = new Intent (v.getContext(), RunSchedulingActivity.class);
                v.getContext().startActivity(intent);
            }
            else if(position == 11){ //toggle music sync on and off
                Intent intent = new Intent (v.getContext(), MusicSyncActivity.class);
                v.getContext().startActivity(intent);
            }
            else if(position == 12){ //edit location access
                Intent intent = new Intent (v.getContext(), LocationAccessActivity.class);
                v.getContext().startActivity(intent);
            }


        }
    }

    //initialize adapter dataset
    public SettingsCustomAdapter(RecyclerData settingsList) {
        settingsOptions = settingsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.settings_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    //associate viewholder with data
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //replace the contents of view with element at same position
        viewHolder.settingTextView.setText(settingsOptions.settings[position]);
    }

    //return size of dataset
    @Override
    public int getItemCount() {
        return settingsOptions.settings.length;
    }




    }






