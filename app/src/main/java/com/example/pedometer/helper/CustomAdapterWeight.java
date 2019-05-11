package com.example.pedometer.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cursoradapter.widget.SimpleCursorAdapter;

import com.example.pedometer.R;
import com.example.pedometer.provider.Provider;

import static com.example.pedometer.helper.RandomMethods.roundAvoidD;
import static com.example.pedometer.helper.RandomMethods.roundAvoidF;


// https://stackoverflow.com/questions/17708971/using-custom-simplecursoradapter
public class CustomAdapterWeight extends SimpleCursorAdapter {

    private Context mContext;
    private Context appContext;
    private int layout;
    private Cursor cr;
    private LayoutInflater inflater;


    public CustomAdapterWeight(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.cr = c;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView date = (TextView) view.findViewById(R.id.StepItemTextView1);
        TextView weight = (TextView) view.findViewById(R.id.StepItemTextView6);

        int CreationDateIndex = cursor.getColumnIndexOrThrow(Provider.Pedometer.TIMESTAMP);
        int WeightIndex = cursor.getColumnIndexOrThrow(Provider.Pedometer.VALUE);


        String mdate = TimeHelper.convertMilliesToString (cursor.getLong(CreationDateIndex));
        String mWeight = cursor.getString(WeightIndex);

        date.setText(mdate);
        weight.setText(""+ mWeight + "kg");
    }

}
