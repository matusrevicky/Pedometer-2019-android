package com.example.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("EXIT", "Service Stops! Oooooooooooooppppssssss!!!!");

        if (android.os.Build.VERSION.SDK_INT >= 28){
            context.startForegroundService(new Intent(context , StepsService.class));
        } else {
        context.startService(new Intent(context , StepsService.class));}
    }
}
