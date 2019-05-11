package com.example.pedometer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.pedometer.fragments.HomeFragment;

public class StepsIntentService extends IntentService {


    public StepsIntentService() {
        super("StepsIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        long millis = SystemClock.elapsedRealtime();
        long minutes = millis / (1000 * 60);

        triggerNotification(minutes);
        triggerBroadcast(minutes);
    }

    // vysielac, v activite sa robi primanie
    private void triggerBroadcast(long minutes) {
        Intent intent = new Intent("Steps");
        intent.putExtra("Steps", minutes);

        LocalBroadcastManager broadcast = LocalBroadcastManager.getInstance(this);
        broadcast.sendBroadcast(intent);

    }

    private void triggerNotification(long uptimeMinutes) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "Steps";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Steps", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Steps")
                .setContentText("Steps " + uptimeMinutes + "steps")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(getPendingIntent())      // ak chcem po kliknuti spustit aktivitu
                .build();

        notificationManager.notify(0, notification);
    }

    private PendingIntent getPendingIntent() {
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        return intent;
    }

}
