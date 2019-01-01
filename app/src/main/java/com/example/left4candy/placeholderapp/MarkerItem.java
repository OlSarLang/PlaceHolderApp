package com.example.left4candy.placeholderapp;

import android.content.res.Resources;

/**
 * Created by shootymcface on 11/28/18.
 */

public class MarkerItem {
    private String header = "";
    private String subHeader = "";

    public MarkerItem(){
        this("", "");
    }

    public MarkerItem(String header, String subHeaderOne){
        this.header = header;
        this.subHeader = subHeaderOne;
    }

    public String getHeader() {
        return header;
    }
    public void setHeader(String header) {
        this.header = header;
    }

    public String getSubHeader() {
        return subHeader;
    }
    public void setSubHeader(String subHeader) {
        this.subHeader = subHeader;
    }
}
