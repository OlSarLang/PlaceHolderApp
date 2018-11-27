package com.example.left4candy.placeholderapp;

import android.widget.ImageView;

public class CustomMarker {

    //What I need to create
    private static int id = 0;
    private int markerId = 1;
    private String markerName;
    private int xPos;
    private int yPos;
    private ImageView image;
    private String solidColor;

    private String header;
    private String subHeaderOne;
    private String subHeaderTwo;
    //TODO Fixa ett sätt att få referens från bilden jag valt för markören och spara den någonstans.


    public CustomMarker(int xPos, int yPos){
        this(0000, "placeholder", xPos, yPos, "yellow");
    }

    public CustomMarker(int markerId, int xPos, int yPos){
        this(markerId, "name", xPos, yPos, "yellow");
    }

    public CustomMarker(int markerId, String markerName, int xPos, int yPos, String solidColor){
        id++;
        this.markerId = id;
        this.markerName = markerName;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public static int getId() {return id;}
    public static void setId(int id) {CustomMarker.id = id;}

    public int getMarkerId() {return markerId;}
    public void setMarkerId(int markerId) {this.markerId = markerId;}

    public String getMarkerName() {return markerName;}
    public void setMarkerName(String markerName) {this.markerName = markerName;}

    public int getxPos() {return xPos;}
    public void setxPos(int xPos) {this.xPos = xPos;}

    public int getyPos() {return yPos;}
    public void setyPos(int yPos) {this.yPos = yPos;}

    public ImageView getImage() {return image;}
    public void setImage(ImageView image) {this.image = image;}

    public String getSolidColor() {return solidColor;}
    public void setSolidColor(String solidColor) {this.solidColor = solidColor;}

    public String getSHeader() {return header;}
    public void setHeader(String header) {this.header = header;}

    public String getSubHeaderOne() {return subHeaderOne;}
    public void setSubHeaderOne(String subHeaderOne) {this.subHeaderOne = subHeaderOne;}

    public String getSubHeaderTwo() {return subHeaderTwo;}
    public void setSubHeaderTwo(String subHeaderTwo) {this.subHeaderTwo = subHeaderTwo;}
}
