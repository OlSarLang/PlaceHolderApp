package com.example.left4candy.placeholderapp;

import android.widget.ImageView;

import java.util.ArrayList;

public class CustomMarker {

    //What I need to create
    private static int id = 0;
    private int markerId = 1;
    private String markerName;
    private int xPos;
    private int yPos;
    private ImageView image;
    private String solidColor;

    private MarkerItem markerItem;

    private ArrayList<MarkerItem> markerItems = new ArrayList<>();
    //TODO Fixa ett sätt att få referens från bilden jag valt för markören och spara den någonstans.


    public CustomMarker(int xPos, int yPos){
        this(0000, "placeholder", xPos, yPos, "yellow");

        markerItem = new MarkerItem("header", "subHeaderOne", "subHeaderTwo");
        markerItems.add(markerItem);
    }

    public CustomMarker(int markerId, int xPos, int yPos){
        this(markerId, "name", xPos, yPos, "yellow");
        markerItem = new MarkerItem("header", "subHeaderOne", "subHeaderTwo");
        markerItems.add(markerItem);
    }

    public CustomMarker(int markerId, String markerName, int xPos, int yPos, String solidColor){
        markerItem = new MarkerItem("header", "subHeaderOne", "subHeaderTwo");
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

    public ImageView getImage() {return image;}
    public void setImage(ImageView image) {this.image = image;}

    public String getSolidColor() {return solidColor;}
    public void setSolidColor(String solidColor) {this.solidColor = solidColor;}

    public MarkerItem getMarkerItem() {
        return markerItem;
    }
    public void setMarkerItem(MarkerItem markerItem) {
        this.markerItem = markerItem;
    }

    public ArrayList<MarkerItem> getMarkerItems() {
        return markerItems;
    }
    public void setMarkerItems(ArrayList<MarkerItem> markerItems) {
        this.markerItems = markerItems;
    }
}
