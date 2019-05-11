package com.example.pedometer.helper;

public class RandomMethods {
    public static float roundAvoidF(float value, int places) {
        double scale = Math.pow(10, places);
        return (float) (Math.round(value * scale) / scale);
    }
    public static double roundAvoidD(double value, int places) {
        double scale = Math.pow(10, places);
        return  (Math.round(value * scale) / scale);
    }
}
