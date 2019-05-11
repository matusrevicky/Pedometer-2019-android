package com.example.pedometer;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.example.pedometer.fragments.HealthFragmentParent;
import com.example.pedometer.fragments.SettingsFragment;
import com.example.pedometer.fragments.StepsListFragmentParent;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;

import com.example.pedometer.fragments.HomeFragment;
import com.example.pedometer.fragments.StepsListFragment;

// https://fabcirablog.weebly.com/blog/creating-a-never-ending-background-service-in-android
public class MainActivity extends AppCompatActivity {

    Intent mServiceIntent;
    private StepsService mSensorService;

    ///////////////////////////// notifications -start
    private MyBroadcastReceiver broadcastReceiver;

    private class MyBroadcastReceiver extends android.content.BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            long uptime = intent.getLongExtra("uptime",0);
           // uptimeTextView.setText(Long.toString(uptime));
        }
    }

    private void schedule() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        PendingIntent intent = PendingIntent.getService(this, 0, new Intent(this, StepsIntentService.class), 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 5*1000, intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.broadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter("uptime");
        LocalBroadcastManager.getInstance(this).registerReceiver(this.broadcastReceiver, intentFilter);
    }
    ///////////////////////////// notifications -end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creates bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // set home fragment, if device is rotated set the last fragment
        if(null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        mSensorService = new StepsService();
        mServiceIntent = new Intent(this,  mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            if (android.os.Build.VERSION.SDK_INT >= 26){
                startForegroundService(new Intent(this , StepsService.class));
            } else {
                startService(new Intent(this , StepsService.class));}
        }

        schedule();

    }

    @Override
    protected void onDestroy() {
      //  stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;
            switch(menuItem.getItemId()){
                case R.id.nav_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.nav_stats:
                    selectedFragment = new StepsListFragmentParent();
                    break;
                case R.id.nav_settings:
                    selectedFragment = new SettingsFragment();
                    break;
                case R.id.health_data:
                    selectedFragment = new HealthFragmentParent();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            return true;
        }
    };

}
