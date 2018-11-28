package com.example.left4candy.placeholderapp;

/**
 * Created by shootymcface on 11/28/18.
 */

public class MarkerItem {

    private String header = "";
    private String subHeaderOne = "";
    private String subHeaderTwo = "";

    public MarkerItem(String header, String subHeaderOne, String subHeaderTwo){
        this.header = header;
        this.subHeaderOne = subHeaderOne;
        this.subHeaderTwo = subHeaderTwo;
    }

    public String getHeader() {
        return header;
    }
    public void setHeader(String header) {
        this.header = header;
    }

    public String getSubHeaderOne() {
        return subHeaderOne;
    }
    public void setSubHeaderOne(String subHeaderOne) {
        this.subHeaderOne = subHeaderOne;
    }

    public String getSubHeaderTwo() {
        return subHeaderTwo;
    }
    public void setSubHeaderTwo(String subHeaderTwo) {
        this.subHeaderTwo = subHeaderTwo;
    }

}
