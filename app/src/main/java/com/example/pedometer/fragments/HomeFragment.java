package com.example.pedometer.fragments;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pedometer.R;
import com.example.pedometer.StepsService;
import com.example.pedometer.provider.PedometerContentProvider;
import com.example.pedometer.provider.Provider;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.PieChartView;

import static com.example.pedometer.helper.RandomMethods.roundAvoidD;
import static com.example.pedometer.helper.RandomMethods.roundAvoidF;

// https://github.com/lecho/hellocharts-android/blob/master/hellocharts-samples/src/lecho/lib/hellocharts/samples/PieChartActivity.java
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private PieChartView chart;
    private PieChartData data;

    private TextView distanceTextView;
    private TextView caloriesTextView;
    private TextView timeTextView;
    private TextView speedTextView;
    private Button pauseButton;


    private boolean hasLabels = true;
    private boolean hasLabelsOutside = true;
    private boolean hasCenterCircle = true;
    private boolean hasCenterText1 = true;
    private boolean hasCenterText2 = true;
    private boolean isExploded = false;
    private boolean hasLabelForSelected = true;


    public int stepsToday = 0;
    private int stepsGoalToday = 0;
    private final int loaderId = 1;
    private int stepLenght = 0;
    private double height = 0;
    private String gender = "";
    private long walkingTimeSeconds = 0;
    private double walkingSpeedMetersPerSecond = 0;
    private double weight;
    private double caloriesBurnedToday;


    public HomeFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        getActivity().setTitle("Today");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        stepsGoalToday = Integer.parseInt(sharedPreferences.getString("goal", "5000"));

        chart = (PieChartView) v.findViewById(R.id.chart);
        chart.setOnValueTouchListener(new ValueTouchListener());

        distanceTextView = v.findViewById(R.id.textViewDistance);
        caloriesTextView = v.findViewById(R.id.textViewCalories);
        timeTextView = v.findViewById(R.id.textViewTime);
        speedTextView = v.findViewById(R.id.textViewSpeed);
        pauseButton = v.findViewById(R.id.button);

        if (StepsService.restartServiceOnDestroy == true) {
            pauseButton.setText("Start");
            Toast.makeText(getActivity(), "Step counting paused", Toast.LENGTH_SHORT).show();
        } else {
            pauseButton.setText("Pause");
            Toast.makeText(getActivity(), "Step counting running", Toast.LENGTH_SHORT).show();
        }

        // there were problems stopping endless service, this somehow works
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StepsService.restartServiceOnDestroy = !StepsService.restartServiceOnDestroy;
                if (StepsService.restartServiceOnDestroy == true) {
                    pauseButton.setText("Start");
                    Toast.makeText(getActivity(), "Step counting paused", Toast.LENGTH_SHORT).show();
                } else {
                    pauseButton.setText("Pause");
                    Toast.makeText(getActivity(), "Step counting running", Toast.LENGTH_SHORT).show();
                }

                if (StepsService.restartServiceOnDestroy == true) {
                    getActivity().stopService(new Intent(getActivity(), StepsService.class));
                } else {
                    getActivity().stopService(new Intent(getActivity(), StepsService.class));
                }
            }
        });

        height = Double.parseDouble(sharedPreferences.getString("height", "170"));
        gender = sharedPreferences.getString("gender", "male");
        weight = Double.parseDouble(sharedPreferences.getString("weight", "50"));

        updateDisCalTime();
        calculateDistanceFromGenderAndHeight();


        // called only if no records of current date are inserted, otherwise insert data from loader
        if (stepsToday == 0) {
            generateData();
        }

        getActivity().getSupportLoaderManager().initLoader(loaderId, Bundle.EMPTY, this);

        return v;
    }


    private void updateDisCalTime() {
        distanceTextView.setText(calculateDistanceFromGenderAndHeight() + "\nkm");
        caloriesTextView.setText(caloriesBurnedToday + "\nCal");
        timeTextView.setText(walkingTimeSeconds + "\nTime");
        speedTextView.setText(walkingSpeedMetersPerSecond + "\nm/s");
    }

    // https://livehealthy.chron.com/determine-stride-pedometer-height-weight-4518.html
    private float calculateDistanceFromGenderAndHeight() {

        if (gender.equals("male")) {
            stepLenght = (int) Math.round(height * 0.415);
        } else {
            stepLenght = (int) Math.round(height * 0.413);
        }
        // cm to km
        return roundAvoidF(stepLenght * stepsToday / (float) 100000, 3);
    }

    // https://www.womanandhome.com/health-and-wellbeing/calories-burned-walking-206766/
    private double calculateBurnedCalories() {
        caloriesBurnedToday = (0.035 * weight) + ((Math.pow(walkingSpeedMetersPerSecond, 2)) / height / 100) * (0.029) * (weight);
        caloriesBurnedToday = caloriesBurnedToday * stepsToday / 60;
        return caloriesBurnedToday = roundAvoidD(caloriesBurnedToday, 3);
    }

    ///////////////////////////////// LoaderManager - start
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = new CursorLoader(getContext());
        // only data from today
        loader.setUri(PedometerContentProvider.CONTENT_URI_ONE);
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            String text = cursor.getString(cursor.getColumnIndex(Provider.Pedometer.STEPS_COUNT));
            stepsToday = Integer.parseInt(text);

            String walTime = cursor.getString(cursor.getColumnIndex(Provider.Pedometer.WALKING_TIME));
            walkingTimeSeconds = Long.parseLong(walTime) / 1000;
            walkingSpeedMetersPerSecond = roundAvoidD(((stepLenght * stepsToday) / 100d) / (Math.max(1, walkingTimeSeconds)), 3);

            calculateBurnedCalories();

            generateData();
            updateDisCalTime();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
    ///////////////////////////////// LoaderManager - end


    //////////////////////////////// pie Chart - start
    private void reset() {
        chart.setCircleFillRatio(1.0f);
        hasLabels = true;
        hasLabelsOutside = false;
        hasCenterCircle = true;
        hasCenterText1 = true;
        hasCenterText2 = true;
        isExploded = false;
        hasLabelForSelected = false;
    }

    // called every time steps are updated
    private void generateData() {
        int numValues = 2;

        List<SliceValue> values = new ArrayList<SliceValue>();

        SliceValue sliceValue = new SliceValue((float) stepsToday, ChartUtils.COLOR_GREEN);
        values.add(sliceValue);
        SliceValue sliceValue1 = new SliceValue((float) Math.max(stepsGoalToday - stepsToday, 0), ChartUtils.COLOR_RED);
        values.add(sliceValue1);


        data = new PieChartData(values);
        data.setHasLabels(hasLabels);
        data.setHasLabelsOnlyForSelected(hasLabelForSelected);
        data.setHasLabelsOutside(hasLabelsOutside);
        data.setHasCenterCircle(hasCenterCircle);

        if (isExploded) {
            data.setSlicesSpacing(24);
        }

        if (hasCenterText1) {
            data.setCenterText1(String.valueOf(stepsToday));

            // Get roboto-italic font.
            //  Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");
            // data.setCenterText1Typeface(tf);

            // Get font size from dimens.xml and convert it to sp(library uses sp values).
            //      data.setCenterText1FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
            //           (int) getResources().getDimension(R.dimen.pie_chart_text1_size)));
        }

        if (hasCenterText2) {
            data.setCenterText2("Goal " + stepsGoalToday);
            //cannot be applied
//            int orientation = getActivity().getResources().getConfiguration().orientation;
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                data.setCenterText1FontSize(15);
//            } else {
//                // In portrait
//            }


            //Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "Roboto-Italic.ttf");

            // data.setCenterText2Typeface(tf);
            //  data.setCenterText2FontSize(ChartUtils.px2sp(getResources().getDisplayMetrics().scaledDensity,
            //          (int) getResources().getDimension(R.dimen.pie_chart_text2_size)));
        }


        chart.setPieChartData(data);
    }

    private void explodeChart() {
        isExploded = !isExploded;
        generateData();

    }

    private void toggleLabelsOutside() {
        // has labels have to be true:P
        hasLabelsOutside = !hasLabelsOutside;
        if (hasLabelsOutside) {
            hasLabels = true;
            hasLabelForSelected = false;
            chart.setValueSelectionEnabled(hasLabelForSelected);
        }

        if (hasLabelsOutside) {
            chart.setCircleFillRatio(0.7f);
        } else {
            chart.setCircleFillRatio(1.0f);
        }

        generateData();

    }

    private void toggleLabels() {
        hasLabels = !hasLabels;

        if (hasLabels) {
            hasLabelForSelected = false;
            chart.setValueSelectionEnabled(hasLabelForSelected);

            if (hasLabelsOutside) {
                chart.setCircleFillRatio(0.7f);
            } else {
                chart.setCircleFillRatio(1.0f);
            }
        }

        generateData();
    }

    private void toggleLabelForSelected() {
        hasLabelForSelected = !hasLabelForSelected;

        chart.setValueSelectionEnabled(hasLabelForSelected);

        if (hasLabelForSelected) {
            hasLabels = false;
            hasLabelsOutside = false;

            if (hasLabelsOutside) {
                chart.setCircleFillRatio(0.7f);
            } else {
                chart.setCircleFillRatio(1.0f);
            }
        }

        generateData();
    }

    /**
     * To animate values you have to change targets values and then call {@link Chart#startDataAnimation()}
     * method(don't confuse with View.animate()).
     */
    private void prepareDataAnimation() {
        for (SliceValue value : data.getValues()) {
            value.setTarget((float) Math.random() * 30 + 15);
        }
    }

    private class ValueTouchListener implements PieChartOnValueSelectListener {

        @Override
        public void onValueSelected(int arcIndex, SliceValue value) {
            Toast.makeText(getActivity(), "Selected: " + value, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onValueDeselected() {
            // TODO Auto-generated method stub

        }

    }

    //////////////////////////////// pie Chart - end

}
