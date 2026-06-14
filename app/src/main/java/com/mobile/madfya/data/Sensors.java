package com.mobile.madfya.data;

public  class Sensors {

    public String firebaseKey;
    public int filterId;

    //Sensor data add more if you need it dani
    public String ph_level;
    public String turbidity;
    public String temperature;
    public String water_flow_rate;
    public String status; //Can be something like WARNING, OPERATIONAL, DISABLED
    public int HP; //Takes the 4 sensors input and averages it.

    //Time
    public long CurrentTimeStamp;
    public long LastMaintenance;

    //Maintenance Stuff
    public String InstalledBy;

    //Location
    public double latitude;
    public double longitude;
    public Sensors() {}
    public Sensors(long currentTimeStamp, long lastMaintenance, String installedBy, double latitude, double longitude) {
        this.CurrentTimeStamp = currentTimeStamp;
        this.LastMaintenance = lastMaintenance;
        this.InstalledBy = installedBy;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}