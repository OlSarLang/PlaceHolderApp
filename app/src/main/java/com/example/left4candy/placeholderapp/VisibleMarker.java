package com.example.left4candy.placeholderapp;

import android.widget.ImageView;

public class VisibleMarker {

    //What I need to create
    private int markerId;
    private int xPos;
    private int yPos;
    private ImageView image;
    private String solidColor;
    //TODO Fixa ett sätt att få referens från bilden jag valt för markören och spara den någonstans.

    public VisibleMarker(int markerId, int xPos, int yPos){
        this.markerId = markerId;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getMarkerId() {return markerId;}
    public void setMarkerId(int markerId) {this.markerId = markerId;}

    public int getxPos() {return xPos;}
    public void setxPos(int xPos) {this.xPos = xPos;}

    public int getyPos() {return yPos;}
    public void setyPos(int yPos) {this.yPos = yPos;}

    public ImageView getImage() {return image;}
    public void setImage(ImageView image) {
        this.image = image;}

    public String getSolidColor() {return solidColor;}
    public void setSolidColor(String solidColor) {this.solidColor = solidColor;}
}