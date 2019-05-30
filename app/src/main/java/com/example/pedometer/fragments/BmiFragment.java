package com.example.pedometer.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pedometer.R;

// based on https://github.com/iamshz97/AndroidBMICalculator
public class BmiFragment extends Fragment {

    private TextView weightTextView;
    private TextView heightTextView;

    private TextView BMITextVIew;
    private ImageView imageView;
    private CalculateBMI calculateBMI;

    public BmiFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_bmi, container, false);
        BMITextVIew = (TextView) v.findViewById(R.id.lblBMI);
        imageView = (ImageView) v.findViewById(R.id.imgbmi);
        weightTextView = v.findViewById(R.id.weightTextView);
        heightTextView = v.findViewById(R.id.heightTextView);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());


            double txtcentimeters = Double.parseDouble(sharedPreferences.getString("height", "175"));
            double txtweight = Double.parseDouble(sharedPreferences.getString("weight", "75"));

            weightTextView.setText("Your weight\n" +txtweight+" kg");
            heightTextView.setText("Your height\n" +txtcentimeters+" cm");

            calculateBMI = new CalculateBMI(txtcentimeters, txtweight);
            // Calculating BMI
            double bmi = calculateBMI.camlculatebmi(calculateBMI.getInputkg(), calculateBMI.getInputCentimeters());

            //Getting BMI Type
            String bmitype = calculateBMI.getbmitype(bmi);

            //Adding to Display Elements
            BMITextVIew.setText("Your BMI\n" +bmi);
            switch (bmitype) {
                case "Underweight":
                    imageView.setImageResource(R.drawable.underweight);
                    break;

                case "Normal Weight":
                    imageView.setImageResource(R.drawable.normal);
                    break;

                case "Over Weight":
                    imageView.setImageResource(R.drawable.overweight);
                    break;

                case "Obesity":
                    imageView.setImageResource(R.drawable.obese);
                    break;

                case "Extremely Obesity":
                    imageView.setImageResource(R.drawable.extremelyobese);
                    break;

                default:
                    imageView.setImageResource(R.drawable.maxresdefault);
                    break;
            }




        return v;
    }

    //////// logic
    public class CalculateBMI {

        private double inputCentimeters;
        private double inputkg;


        CalculateBMI(double centimeters, double inputkg) {
            this.inputCentimeters = centimeters;
            this.inputkg = inputkg;
        }

        double getInputCentimeters() {
            return inputCentimeters;
        }

        double getInputkg() {
            return inputkg;
        }

        double camlculatebmi(double inputkg, double inputCentimeters) {
            double result = 0;

            double txtheightm = (inputCentimeters) / 100;

            result = inputkg / (txtheightm * txtheightm);

            result = (double) Math.round(result * 100) / 100;

            return result;


        }

        String getbmitype(double bmi) {
            String type = "null";

            if (bmi <= 18.5) {
                type = "Underweight";
            } else if (bmi <= 24.9) {
                type = "Normal Weight";
            } else if (bmi <= 29.9) {
                type = "Over Weight";
            } else if (bmi <= 34.9) {
                type = "Obesity";
            } else if (bmi > 35) {
                type = "Extremely Obesity";
            }

            return type;

        }
    }

}

