package com.example.courseproject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.lang.reflect.InvocationTargetException;

//sends notification when it is time for future run
public class RunNotificationService extends Service {
    public RunNotificationService() {
    }

    private String message, goal;
    int requestCode;

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {  //create notification
        message = intent.getExtras().getString("message");  //get message
        goal = intent.getExtras().getString("goal");  //get key
        requestCode = intent.getExtras().getInt("code");    //get request code

        //explicit intent to current run activity
        Intent i = new Intent(this, CurrentRunActivity.class);
        i.putExtra("goal", goal);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode, i, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifyRun")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("It's time to RunItUp")
                .setContentText("you said: " +message+ "  run goal: " +goal+ " km")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat nManager = NotificationManagerCompat.from(this);
        nManager.notify(500, builder.build());

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}