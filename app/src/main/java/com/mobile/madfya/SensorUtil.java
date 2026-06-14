package com.mobile.madfya;

public class SensorUtil {
    public static String getSensorName(int filterId) {
        switch (filterId) {
            case 1: return "Main Filter";
            case 2: return "Sub Filter A";
            case 3: return "Sub Filter B";
            case 4: return "Sub Filter C";
            default: return "Unknown";
        }
    }
}
