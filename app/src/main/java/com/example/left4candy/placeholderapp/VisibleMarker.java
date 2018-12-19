package com.example.left4candy.placeholderapp;

import android.graphics.Bitmap;
import android.widget.ImageView;

public class VisibleMarker {

    //What I need to create
    private int markerId;
    private float xPos;
    private float yPos;
    private ImageView image;
    private Bitmap solidColor;
    //TODO Fixa ett sätt att få referens från bilden jag valt för markören och spara den någonstans.

    public VisibleMarker(int markerId, float xPos, float yPos){
        this.markerId = markerId;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getMarkerId() {return markerId;}
    public void setMarkerId(int markerId) {this.markerId = markerId;}

    public float getxPos() {return xPos;}
    public void setxPos(float xPos) {this.xPos = xPos;}

    public float getyPos() {return yPos;}
    public void setyPos(float yPos) {this.yPos = yPos;}

    public ImageView getImage() {return image;}
    public void setImage(ImageView image) {
        this.image = image;}

    public Bitmap getSolidColor() {return solidColor;}
    public void setSolidColor(Bitmap solidColor) {this.solidColor = solidColor;}
}