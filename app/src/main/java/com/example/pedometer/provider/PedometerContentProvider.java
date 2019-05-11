package com.example.pedometer.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.pedometer.Defaults;
import com.example.pedometer.helper.TimeHelper;


public class PedometerContentProvider extends ContentProvider {
    public static final String AUTHORITY
            = "com.example.pedometer.provider.PedometerContentProvider";

    // all steps
    public static final Uri CONTENT_URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(Provider.Pedometer.TABLE_STEPS_SUMMARY)
            .build();

    // all steps
    public static final Uri CONTENT_URI_WEEKS = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(Provider.Pedometer.TABLE_STEPS_SUMMARY)
            .appendPath("all")
            .build();

    // steps from today
    public static final Uri CONTENT_URI_ONE = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(Provider.Pedometer.TABLE_STEPS_SUMMARY)
            .appendPath(TimeHelper.getTodayDate()+"")
            .build();

    // weight tracker weight all
    public static final Uri CONTENT_URI_WEIGHT = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(AUTHORITY)
            .appendPath(Provider.Pedometer.TABLE_WEIGHT)
            .build();

//    public static final Uri CONTENT_URI_ONE
//            = Uri.parse("content://com.example.pedometer.provider.PedometerContentProvider/StepsSummary/4");


    private StepsDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        this.dbHelper = new StepsDBHelper(getContext());

        uriMatcher.addURI(AUTHORITY, Provider.Pedometer.TABLE_WEIGHT, URI_MATCH_WEIGHT);
        uriMatcher.addURI(AUTHORITY, Provider.Pedometer.TABLE_STEPS_SUMMARY, URI_MATCH_STEPS);
        uriMatcher.addURI(AUTHORITY, Provider.Pedometer.TABLE_STEPS_SUMMARY + "/#", URI_MATCH_STEPS_BY_DATE);
        uriMatcher.addURI(AUTHORITY, Provider.Pedometer.TABLE_STEPS_SUMMARY + "/all", URI_MATCH_STEPS_WEEKS);

        return true;
    }

    // to get one entry from db
    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int URI_MATCH_STEPS = 0;
    private static final int URI_MATCH_STEPS_BY_DATE = 1;
    private static final int URI_MATCH_WEIGHT = 2;
    private static final int URI_MATCH_STEPS_WEEKS = 3;


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;

        switch (uriMatcher.match(uri)) {
            case URI_MATCH_STEPS:
                cursor = listSteps();
                return cursor;
            case URI_MATCH_STEPS_WEEKS:
                cursor = listStepsWeeks();
                return cursor;
            case URI_MATCH_STEPS_BY_DATE:
                long id = ContentUris.parseId(uri);
                cursor = findByDate(id);
                return cursor;
            case URI_MATCH_WEIGHT:
                cursor = listWeight();
                return cursor;
            default:
                return Defaults.NO_CURSOR;
        }
    }

    private Cursor listWeight() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Provider.Pedometer.TABLE_WEIGHT,
                Defaults.ALL_COLUMNS,
                Defaults.NO_SELECTION,
                Defaults.NO_SELECTION_ARGS,
                Defaults.NO_GROUP_BY,
                Defaults.NO_HAVING,
                Provider.Pedometer.TIMESTAMP+ " DESC");  // sorted
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI_WEIGHT);
        return cursor;
    }

    private Cursor listSteps() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(Provider.Pedometer.TABLE_STEPS_SUMMARY,
                Defaults.ALL_COLUMNS,
                Defaults.NO_SELECTION,
                Defaults.NO_SELECTION_ARGS,
                Defaults.NO_GROUP_BY,
                Defaults.NO_HAVING,
                Provider.Pedometer.CREATION_DATE+ " DESC");
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        return cursor;
    }

    private Cursor listStepsWeeks() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select _id, sum(StepsCount) as stepsCount, strftime('%W', datetime(creationdate/1000, 'unixepoch','localtime')) as weekNumber, sum(walkingtime/1000) as walkingTime from StepsSummary group by weekNumber;",null);
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI_WEEKS);
        return cursor;
    }

    private Cursor findByDate(long date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = Provider.Pedometer.CREATION_DATE + "=" + date;
        Cursor cursor = db.query(Provider.Pedometer.TABLE_STEPS_SUMMARY,
                Defaults.ALL_COLUMNS,
                selection,
                Defaults.NO_SELECTION_ARGS,
                Defaults.NO_GROUP_BY,
                Defaults.NO_HAVING,
                Defaults.NO_SORT_ORDER);
        cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI_ONE);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri newUri = null;

        switch (uriMatcher.match(uri)) {
            case URI_MATCH_STEPS:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = db.insert(Provider.Pedometer.TABLE_STEPS_SUMMARY, Defaults.NO_NULL_COLUMN_HACK, values);
                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.notifyChange(CONTENT_URI, Defaults.NO_CONTENT_OBSERVER);
                newUri =  ContentUris.withAppendedId(CONTENT_URI, id);
                return newUri;
            case URI_MATCH_WEIGHT:
                SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                long id1 = db1.insert(Provider.Pedometer.TABLE_WEIGHT, Defaults.NO_NULL_COLUMN_HACK, values);
                ContentResolver contentResolver1 = getContext().getContentResolver();
                contentResolver1.notifyChange(CONTENT_URI_WEIGHT, Defaults.NO_CONTENT_OBSERVER);
                newUri =  ContentUris.withAppendedId(CONTENT_URI_WEIGHT, id1);
                return newUri;
            default:
                return newUri;
        }

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (uriMatcher.match(uri)) {
            case URI_MATCH_STEPS:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int rows = db.delete(Provider.Pedometer.TABLE_STEPS_SUMMARY, selection, selectionArgs);

                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.notifyChange(CONTENT_URI, Defaults.NO_CONTENT_OBSERVER);

                return rows;
            case URI_MATCH_WEIGHT:
                SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                int rows1 = db1.delete(Provider.Pedometer.TABLE_WEIGHT, selection, selectionArgs);

                ContentResolver contentResolver1 = getContext().getContentResolver();
                contentResolver1.notifyChange(CONTENT_URI_WEIGHT, Defaults.NO_CONTENT_OBSERVER);

                return rows1;
            default:
                return 0;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        switch (uriMatcher.match(uri)) {
            case URI_MATCH_STEPS:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                int rows = db.update(Provider.Pedometer.TABLE_STEPS_SUMMARY, values, selection, selectionArgs);

                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.notifyChange(CONTENT_URI, Defaults.NO_CONTENT_OBSERVER);
                return rows;
            case URI_MATCH_WEIGHT:
                SQLiteDatabase db1 = dbHelper.getWritableDatabase();
                int rows1 = db1.update(Provider.Pedometer.TABLE_WEIGHT, values, selection, selectionArgs);

                ContentResolver contentResolver1 = getContext().getContentResolver();
                contentResolver1.notifyChange(CONTENT_URI_WEIGHT, Defaults.NO_CONTENT_OBSERVER);
                return rows1;
            default:
                return 0;
        }


    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}