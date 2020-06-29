package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetSearchStops {
    @SerializedName("id")
    private int id;
    @SerializedName("busline")
    private String busline;
    @SerializedName("busname")
    private String busname;
    @SerializedName("busdire")
    private String busdire;
    @SerializedName("busnum")
    private int busnum;

    public int getId() {
        return id;
    }

    public String getBusline() {return busline;}

    public String getBusname() {
        return busname;
    }

    public String getBusdire() {
        return busdire;
    }

    public int getBusnum() {
        return busnum;
    }

}
