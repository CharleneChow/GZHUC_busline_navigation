package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetStopXY {
    @SerializedName("ID")
    private int id;
    @SerializedName("Bus_SName")
    private String busSn;
    @SerializedName("Bus_SPointN")
    private String busPointN;
    @SerializedName("Bus_SPointE")
    private String busPointE;
    @SerializedName("Bus_Direction")
    private String busDirection;

    public int getId() {
        return id;
    }

    public String getBusSn() {
        return busSn;
    }

    public String getBusPointN() {
        return busPointN;
    }

    public String getBusPointE() {
        return busPointE;
    }

    public String getBusDirection() {
        return busDirection;
    }
}
