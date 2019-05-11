package com.example.pedometer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.pedometer.Defaults;


// some code from https://hub.packtpub.com/step-detector-and-step-counters-sensors/#more
public class StepsDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StepsDatabase";

    public StepsDBHelper(Context context) {
        super(context, DATABASE_NAME, Defaults.DEFAULT_CURSOR_FACTORY, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Provider.Pedometer.CREATE_TABLE_STEPS_SUMMARY);
        db.execSQL(Provider.Pedometer.CREATE_TABLE_WEIGHT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    // not yet implementeted
    }



}