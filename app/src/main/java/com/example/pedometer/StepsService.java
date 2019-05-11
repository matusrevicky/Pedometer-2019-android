package com.example.pedometer;

import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.example.pedometer.helper.TimeHelper;
import com.example.pedometer.provider.PedometerContentProvider;
import com.example.pedometer.provider.Provider;
import com.example.pedometer.provider.StepsDBHelper;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;


// based on https://hub.packtpub.com/step-detector-and-step-counters-sensors/#more
public class StepsService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mStepDetectorSensor;
    private StepsDBHelper mStepsDBHelper;

    public int counter = 0;

    private long lastStepTimeMillies = 0;
    private long thisStepTimeMillies = 0;


    public StepsService() {
        super();
        Log.i("HERE", "here I am!");
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mStepsDBHelper = new StepsDBHelper(this);
        }
    }


    // method is called after every step
    // it either creates a new entry or adds 1 to existing entry
    public boolean createStepsEntry() {

        // walking time calculation -start
        long currentWalkingTimeMillies = 0;
        long walkingTimeDifference = 0;
        if (thisStepTimeMillies - lastStepTimeMillies < 2000) {
            Log.i("STEP",walkingTimeDifference+"");
            walkingTimeDifference =  (thisStepTimeMillies - lastStepTimeMillies);
        }

        lastStepTimeMillies = thisStepTimeMillies;
        thisStepTimeMillies = System.currentTimeMillis();
        // walking time calculation -end

        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;
        int currentDateStepCounts = 0;
        long todayDate = TimeHelper.getTodayDate();


        // gets stepcount from today, if exists
        try {
            SQLiteDatabase db = mStepsDBHelper.getReadableDatabase();
            Cursor c = db.query(Provider.Pedometer.TABLE_STEPS_SUMMARY, new String[]{Provider.Pedometer.STEPS_COUNT, Provider.Pedometer.WALKING_TIME},
                    Provider.Pedometer.CREATION_DATE + " = ?", new String[]{todayDate + ""}, null, null, null);
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true;
                    currentDateStepCounts = c.getInt((c.getColumnIndex(Provider.Pedometer.STEPS_COUNT)));
                    currentWalkingTimeMillies = c.getLong((c.getColumnIndex(Provider.Pedometer.WALKING_TIME)));
                    Log.i("testovanie","current "+ currentWalkingTimeMillies);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // adds one or creates new entry
        try {
            SQLiteDatabase db = mStepsDBHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Provider.Pedometer.CREATION_DATE, todayDate);
            if (isDateAlreadyPresent) {
                values.put(Provider.Pedometer.STEPS_COUNT, ++currentDateStepCounts);
                values.put(Provider.Pedometer.WALKING_TIME, currentWalkingTimeMillies + walkingTimeDifference);
                AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {};
                handler.startUpdate(0, null, PedometerContentProvider.CONTENT_URI, values, Provider.Pedometer.CREATION_DATE + " =?", new String[]{todayDate + ""});
                db.close();
            } else {
                values.put(Provider.Pedometer.STEPS_COUNT, 1);
                values.put(Provider.Pedometer.WALKING_TIME, 0);
                AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {};
                handler.startInsert(0, null, PedometerContentProvider.CONTENT_URI, values);
                db.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return createSuccessful;
    }

    // just to group data that belongs together
    class DateStepsModel {

        public String mDate;
        public int mStepCount;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int
            startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        this.createStepsEntry();

    }

    @Override
    public void onDestroy() {

        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent(getApplicationContext(), SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("EXIT", "task removed!");
        Intent broadcastIntent = new Intent(getApplicationContext(), SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}