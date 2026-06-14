package com.mobile.madfya;

public class SensorLog {
    public int filterId;
    public String filterName;
    public String gpsCoords;
    public String date;
    public String time;
    public double ph;
    public double turbidity;
    public double temperature;
    public double usage;
    public double latitude;
    public double longitude;

    public SensorLog(int filterId, String filterName, String gpsCoords, String date, String time,
                     double ph, double turbidity, double temperature, double usage,
                     double latitude, double longitude) {
        this.filterId = filterId; // add this
        this.filterName = filterName;
        this.gpsCoords = gpsCoords;
        this.date = date;
        this.time = time;
        this.ph = ph;
        this.turbidity = turbidity;
        this.temperature = temperature;
        this.usage = usage;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}