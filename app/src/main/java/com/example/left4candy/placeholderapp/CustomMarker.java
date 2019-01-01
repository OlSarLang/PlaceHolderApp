package com.example.left4candy.placeholderapp;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class CustomMarker {

    //What I need to create
    private static int id = 1;
    private int markerId = 1;
    private String markerName;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private boolean isSolid = true;
    private boolean red, green, yellow, blue;
    private String header;
    private String subHeaderOne;

    private MarkerItem markerItem;

    private List<MarkerItem> markerItems = new ArrayList<>();
    //TODO Fixa ett sätt att få referens från bilden jag valt för markören och spara den någonstans.

    public CustomMarker(){
        id++;
    }

    public CustomMarker(double latitude, double longitude){
        this(0000, "placeholder", latitude,longitude, true);
        id++;
    }

    public CustomMarker(int markerId, LatLng latLng){
        this(markerId, "name", 0,0, true);
        id++;
    }

    public CustomMarker(int markerId, String markerName, double latitude, double longitude, boolean red){
        id++;
        this.red = red;
        this.markerId = id;
        this.markerName = markerName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static int getId() {return id;}
    public static void setId(int id) {CustomMarker.id = id;}

    public int getMarkerId() {return markerId;}
    public void setMarkerId(int markerId) {this.markerId = markerId;}

    public String getMarkerName() {return markerName;}
    public void setMarkerName(String markerName) {this.markerName = markerName;}

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public MarkerItem getMarkerItem() {
        return markerItem;
    }
    public void setMarkerItem(MarkerItem markerItem) {
        this.markerItem = markerItem;
    }

    public List<MarkerItem> getMarkerItems() {
        return markerItems;
    }
    public void setMarkerItems(List<MarkerItem> markerItems) {
        this.markerItems = markerItems;
    }

    public String getHeader() {return header;}
    public void setHeader(String header) {this.header = header;}

    public String getSubHeaderOne() {return subHeaderOne;}
    public void setSubHeaderOne(String subHeaderOne) {this.subHeaderOne = subHeaderOne;}

    public boolean isRed() {return red; }
    public void setRed(boolean red) {this.red = red; }

    public boolean isGreen() {return green; }
    public void setGreen(boolean green) {this.green = green;}

    public boolean isYellow() {return yellow;}
    public void setYellow(boolean yellow) {this.yellow = yellow;}

    public boolean isBlue() {return blue;}
    public void setBlue(boolean blue) {this.blue = blue; }

    public boolean isSolid() { return isSolid; }
    public void setSolid(boolean solid) { isSolid = solid; }
}
