package com.example.pedometer.provider;

import android.provider.BaseColumns;

// some code from https://hub.packtpub.com/step-detector-and-step-counters-sensors/#more
public interface Provider {
    interface Pedometer extends BaseColumns {

        // steps table
        String TABLE_STEPS_SUMMARY = "StepsSummary";
        String STEPS_COUNT = "stepscount";
        String WALKING_TIME = "walkingtime";
        String CREATION_DATE = "creationdate";//Date format is long miliseconds

        // fields for the database
        String TABLE_WEIGHT = "tracker";
        String VALUE = "value";
        String TIMESTAMP = "timestamp";

        // creates table with 3 columns
        String CREATE_TABLE_STEPS_SUMMARY = String.format("CREATE TABLE " + TABLE_STEPS_SUMMARY + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s INTEGER )",
                BaseColumns._ID, CREATION_DATE, STEPS_COUNT, WALKING_TIME);

        String CREATE_TABLE_WEIGHT = String.format("CREATE TABLE " + TABLE_WEIGHT + "(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER )",
                BaseColumns._ID, VALUE, TIMESTAMP);

    }
}
