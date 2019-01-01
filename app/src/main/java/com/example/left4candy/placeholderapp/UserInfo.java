package com.example.left4candy.placeholderapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class UserInfo {

    private double latitude;
    private double longitude;
    private String userName;
    private boolean privacy;
    private String phoneNumber;
    private String email;


    public UserInfo(){
        this(0,0,"unknown", false);
    }

    public UserInfo(double latitude, double longitude, String userName, boolean privacy){
        this.latitude = latitude;
        this.longitude = longitude;
        this.userName = userName;
        this.privacy = privacy;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isPrivacy() { return privacy; }
    public void setPrivacy(boolean privacy) { this.privacy = privacy; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
