package com.example.left4candy.placeholderapp;

/**
 * Created by shootymcface on 11/28/18.
 */

public class MarkerItem {

    private String header = "empty";
    private String subHeader = "empty";

    public MarkerItem(){
        this("empty", "empty");
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
