package com.mobile.madfya.data;

public class Reports {

    public String firebaseKey;
    public String id;

    public String title;
    public String description;
    public String category;
    //Can be either "Damaged Pipes", "Unusual Behavior", "Miscellaneous"

    //Time
    public long ReportedTimeStamps;

    //Maintenance Stuff
    public String ReportedBy;

    //Location
    public double latitude;
    public double longitude;

    public double ph;
    public double turbidity;
    public double temperature;
    public double usage;
    public String ImagePath;
    public Reports() {}
    public Reports(String title, String description, String category, String reportedBy, double latitude, double longitude, String imagePath) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.ReportedBy = reportedBy;
        this.ReportedTimeStamps = System.currentTimeMillis();
        this.latitude = latitude;
        this.longitude = longitude;
        this.ImagePath = imagePath;
    }
}
