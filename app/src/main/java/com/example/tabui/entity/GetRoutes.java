package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetRoutes {
    @SerializedName("id")
    private int id;
    @SerializedName("brName")
    private String brName;
    @SerializedName("stopName")
    private String stopName;
    @SerializedName("stopNum")
    private String stopNum;
    @SerializedName("stopDire")
    private String stopDire;
    @SerializedName("pointN")
    private String pointN;
    @SerializedName("pointE")
    private String pointE;

    public int getId() {
        return id;
    }

    public String getBrName() {return brName;}

    public String getStopName() {
        return stopName;
    }

    public String getStopNum() {
        return stopNum;
    }

    public String getStopDire() {
        return stopDire;
    }

    public String getPointN() {return pointN;}

    public String getPointE() {return pointE;}
}
