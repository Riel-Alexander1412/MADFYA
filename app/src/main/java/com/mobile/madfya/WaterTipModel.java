package com.mobile.madfya;

public class WaterTipModel {
    private String title;
    private String description;
    private int imageResource;
    private String youtubeUrl;

    public WaterTipModel(String title, String description, int imageResource, String youtubeUrl){
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.youtubeUrl = youtubeUrl;
    }

    public String getTitle(){

        return title;
    }

    public String getDescription(){

        return description;
    }

    public int getImageResource(){

        return imageResource;
    }

    public String getYoutubeUrl(){
        return youtubeUrl;
    }

}
