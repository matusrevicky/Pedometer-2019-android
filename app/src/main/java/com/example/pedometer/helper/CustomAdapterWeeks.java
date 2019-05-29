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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static com.example.pedometer.helper.RandomMethods.roundAvoidD;
import static com.example.pedometer.helper.RandomMethods.roundAvoidF;


// https://stackoverflow.com/questions/17708971/using-custom-simplecursoradapter
public class CustomAdapterWeeks extends SimpleCursorAdapter {

    private Context mContext;
    private Context appContext;
    private int layout;
    private Cursor cr;
    private LayoutInflater inflater;
    private double height;
    private String gender;
    private double weight;
    private int stepLenght;
    private double walkingSpeedMetersPerSecond;

    public CustomAdapterWeeks(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
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
        TextView steps = (TextView) view.findViewById(R.id.StepItemTextView6);
        TextView calories = (TextView) view.findViewById(R.id.StepItemTextView4);
        TextView distance = (TextView) view.findViewById(R.id.StepItemTextView2);
        TextView speed = (TextView) view.findViewById(R.id.StepItemTextView3);
        TextView time = (TextView) view.findViewById(R.id.StepItemTextView5);

        int CreationDateIndex = cursor.getColumnIndexOrThrow("weekNumber");
        int StepsCountIndex = cursor.getColumnIndexOrThrow("stepsCount");
        int TimeCountIndex = cursor.getColumnIndexOrThrow("walkingTime");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        height = Double.parseDouble(sharedPreferences.getString("height", "170"));
        gender = sharedPreferences.getString("gender", "male");
        weight = Double.parseDouble(sharedPreferences.getString("weight", "50"));


        String mdate = cursor.getString(CreationDateIndex);
        String stepC = cursor.getString(StepsCountIndex);
        String timeC = cursor.getString(TimeCountIndex);

        String temp = getWeek(mdate);


        date.setText("week: "+temp );
        steps.setText("Steps:"+ stepC);
        distance.setText(calculateDistanceFromGenderAndHeight(Integer.parseInt(stepC))+"");
        speed.setText(calculateSpeed(Integer.parseInt(stepC),stepLenght,Long.parseLong(timeC))+"");
        calories.setText(calculateBurnedCalories(Integer.parseInt(stepC))+"");
        time.setText(Long.parseLong(timeC) +"");
    }

    private String getWeek(String mdate) {
        DateTime dt = new DateTime()
                .withWeekyear(2019)
                .withWeekOfWeekyear(Integer.parseInt(mdate)+1)
                .withDayOfWeek(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy");
        return (dateTimeFormatter.print(dt));
    }

    // https://livehealthy.chron.com/determine-stride-pedometer-height-weight-4518.html
    private float calculateDistanceFromGenderAndHeight(int stepsCount) {
        if (gender.equals("male")) {
            stepLenght = (int) Math.round(height * 0.415);
        } else {
            stepLenght = (int) Math.round(height * 0.413);
        }
        // cm to km
        return roundAvoidF(stepLenght * stepsCount / (float) 100000, 3);
    }

    private double calculateSpeed(int stepsCount, int stepLenght, long walkingTimeSeconds) {
       return walkingSpeedMetersPerSecond = roundAvoidD(((stepLenght*stepsCount)/100d)/(Math.max(1,walkingTimeSeconds)),3);
    }

    // https://www.womanandhome.com/health-and-wellbeing/calories-burned-walking-206766/
    private double calculateBurnedCalories(int stepsCount) {
        double caloriesBurnedToday = (0.035 * weight ) + ((Math.pow(walkingSpeedMetersPerSecond,2)) / height/100) * (0.029) * (weight);
        caloriesBurnedToday = caloriesBurnedToday * stepsCount/60;
        return caloriesBurnedToday = roundAvoidD(caloriesBurnedToday, 3);
    }


}
