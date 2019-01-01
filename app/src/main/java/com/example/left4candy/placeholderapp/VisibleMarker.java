package com.example.left4candy.placeholderapp;

import android.graphics.Bitmap;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class VisibleMarker{

    //What I need to create
    private int markerId;
    private LatLng latLng;
    private ImageView image;
    private Bitmap solidColor;
    private ImageButton imageCircle;

    public VisibleMarker(int markerId, LatLng latLng){
        this.markerId = markerId;
        this.latLng = latLng;
    }

    public int getMarkerId() {return markerId;}
    public void setMarkerId(int markerId) {this.markerId = markerId;}

    public LatLng getLatLng() {return latLng;}
    public void setLatLng(LatLng latLng) {this.latLng = latLng;}

    public ImageView getImage() {return image;}
    public void setImage(ImageView image) {
        this.image = image;}

    public Bitmap getSolidColor() {return solidColor;}
    public void setSolidColor(Bitmap solidColor) {this.solidColor = solidColor;}

    public ImageButton getImageCircle() {
        return imageCircle;
    }

    public void setImageCircle(ImageButton imageCircle) {
        this.imageCircle = imageCircle;
    }
}