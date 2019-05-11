package com.example.pedometer.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.text.Editable;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.pedometer.Defaults;
import com.example.pedometer.R;
import com.example.pedometer.helper.CustomAdapterWeight;
import com.example.pedometer.helper.TimeHelper;
import com.example.pedometer.provider.PedometerContentProvider;
import com.example.pedometer.provider.Provider;
import com.example.pedometer.provider.StepsDBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;
import java.util.Locale;

// whole fragment based on https://github.com/doubleblacksoftware/simple-weight-tracker
public class GraphWeightDataFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener, AdapterView.OnItemClickListener {

    private GridView weightGridView;
    private CustomAdapterWeight gridViewAdapter;
    StepsDBHelper mStepsDBHelper;

    public GraphWeightDataFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_health, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        mStepsDBHelper = new StepsDBHelper(getActivity());
        /////////// loads data into grid - start
        String[] from = {Provider.Pedometer.TIMESTAMP, Provider.Pedometer.VALUE};
        int[] to = {R.id.StepItemTextView1, R.id.StepItemTextView6};


        weightGridView = v.findViewById(R.id.weightGridView);
        gridViewAdapter = new CustomAdapterWeight(getContext(),
                R.layout.weight_item, Defaults.NO_CURSOR, from, to, Defaults.NO_FLAGS);
        weightGridView.setAdapter(gridViewAdapter);

        weightGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        weightGridView.setMultiChoiceModeListener(this);

        getActivity().getSupportLoaderManager().initLoader(3, Bundle.EMPTY, this);
        /////////// loads data into grid - end

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEntry();
            }
        });
        weightGridView.setOnItemClickListener(this);

        return v;
    }

    public void addEntry() {
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("New Weight")
                .setView(input)
                .setNeutralButton(getResources().getString(R.string.add_with_current_timestamp), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add a new record
                        Editable value = input.getText();
                        storeEntry(value.toString(), TimeHelper.getTodayDate() );
                    }
                })
                .setPositiveButton(getResources().getString(R.string.add_with_custom_date), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Add a new record
                        final Editable value = input.getText();
                        dialog.dismiss();
                        DatePickerFragment newFragment = DatePickerFragment.newInstance( new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, final int year, final int month, final int day) {

                                Calendar cal = Calendar.getInstance();
                                cal.set(year, month, day);
                                cal.set(Calendar.HOUR_OF_DAY, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);
                                storeEntry(value.toString(), cal.getTime().getTime() );
                            }
                        });
                        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                    }
                }).create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void storeEntry(String value, Long timestamp) {

        if (timestamp > System.currentTimeMillis()) {
            Toast.makeText(getActivity(), "Cannot set weight in future", Toast.LENGTH_SHORT).show();
            return;
        }


        // gets weight from today, if exists
        try {
            SQLiteDatabase db = mStepsDBHelper.getReadableDatabase();
            Cursor c = db.query(Provider.Pedometer.TABLE_WEIGHT, new String[]{Provider.Pedometer.TIMESTAMP},
                    Provider.Pedometer.TIMESTAMP + " = ?", new String[]{timestamp+ ""}, null, null, null, null);
            if (c.moveToFirst()) {
                do {
                    long currentWalkingTimeMillies = c.getLong((c.getColumnIndex(Provider.Pedometer.TIMESTAMP)));
                    Log.i("time",""+currentWalkingTimeMillies);
                    Toast.makeText(getActivity(), "Value from date exists", Toast.LENGTH_SHORT).show();
                    return;
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (timestamp == TimeHelper.getTodayDate()){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("weight", value);
            editor.apply();
        }

        ContentValues values = new ContentValues();
        values.put(Provider.Pedometer.VALUE, value);
        values.put(Provider.Pedometer.TIMESTAMP, timestamp);

        AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {};
        handler.startInsert( 0, null, PedometerContentProvider.CONTENT_URI_WEIGHT, values);

    }

    ///////////////////////////////// LoaderManager - start
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = new CursorLoader(getContext());
        loader.setUri(PedometerContentProvider.CONTENT_URI_WEIGHT);

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        gridViewAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        gridViewAdapter.swapCursor(Defaults.NO_CURSOR);
    }
    ///////////////////////////////// LoaderManager - end

    ///////////////////////////////// MultiChoiceModeListener - start
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        int count = weightGridView.getCheckedItemCount();
        actionMode.setSubtitle("Selected items " + count);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.note_cab, menu);
        mode.setTitle("Items selected");
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.deleteNoteMenuItem) {
            for (long id : weightGridView.getCheckedItemIds()) {
                AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {};
                handler.startDelete(0, null,PedometerContentProvider.CONTENT_URI_WEIGHT, "_ID = ?", new String[]{id+""});
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }

    //////////////////////////////// MultiChoiceModeListener - end


    //////////////////////////////// On item click listenter - start
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
        final EditText editText = new EditText(getContext());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        editText.setText(cursor.getString(cursor.getColumnIndex(Provider.Pedometer.VALUE)));
        final String time= cursor.getString(cursor.getColumnIndex(Provider.Pedometer.TIMESTAMP));

        new AlertDialog.Builder(getContext())
                .setTitle("Edit weight")
                .setView(editText)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ContentValues row = new ContentValues();
                        row.put(Provider.Pedometer.VALUE, editText.getText().toString());
                        AsyncQueryHandler handler = new AsyncQueryHandler( getActivity().getContentResolver()) {};
                        handler.startUpdate(0, null, PedometerContentProvider.CONTENT_URI_WEIGHT, row,"_ID = ?", new String[]{id+""});

                        // if today value is updated change in preferences
                        if (Long.parseLong(time) == TimeHelper.getTodayDate()){
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("weight", editText.getText().toString());
                            editor.apply();
                        }

                    }
                }).show();



    }
    //////////////////////////////// On item click listenter - end


    public static class DatePickerFragment extends DialogFragment {
        private DatePickerDialog.OnDateSetListener onDateSetListener;

        static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener onDateSetListener) {
            DatePickerFragment pickerFragment = new DatePickerFragment();
            pickerFragment.setOnDateSetListener(onDateSetListener);
            return pickerFragment;
        }

        private void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
            this.onDateSetListener = listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
        }
    }

}
