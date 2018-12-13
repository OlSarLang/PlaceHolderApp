package com.example.left4candy.placeholderapp;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class CustomMarker {

    //What I need to create
    private static int id = 0;
    private int markerId = 1;
    private String markerName;
    private int xPos;
    private int yPos;
    private String imageUrl;
    private String solidColor;
    private String header;
    private String subHeaderOne;
    private String subHeaderTwo;


    private MarkerItem markerItem;

    private List<MarkerItem> markerItems = new ArrayList<>();
    //TODO Fixa ett sätt att få referens från bilden jag valt för markören och spara den någonstans.


    public CustomMarker(int xPos, int yPos){
        this(0000, "placeholder", xPos, yPos, "yellow");
        markerItem = new MarkerItem("header", "subHeader");
        markerItems.add(markerItem);
    }

    public CustomMarker(int markerId, int xPos, int yPos){
        this(markerId, "name", xPos, yPos, "yellow");
        markerItem = new MarkerItem("header", "subHeader");
        markerItems.add(markerItem);
    }

    public CustomMarker(int markerId, String markerName, int xPos, int yPos, String solidColor){
        markerItem = new MarkerItem("header", "subHeader");
        markerItems.add(markerItem);
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

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public String getSolidColor() {return solidColor;}
    public void setSolidColor(String solidColor) {this.solidColor = solidColor;}

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
}
