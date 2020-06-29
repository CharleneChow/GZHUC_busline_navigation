package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetDisplayRoutes {
    @SerializedName("id")
    private int id;
    @SerializedName("busLine")
    private String busLine;
    @SerializedName("busSName")
    private String busSName;
    @SerializedName("busNextDire")
    private String busNextDire;
    @SerializedName("busNextSName")
    private String busNextSName;
    @SerializedName("busNumPoint")
    private String busNumPoint;
    @SerializedName("busPointE")
    private String busPointE;
    @SerializedName("busPointN")
    private String busPointN;

    public int getID() {
        return id;
    }

    public String getBusLine() {return busLine;}

    public String getBusSName() {return busSName;
    }
    public String getBusNextDire() {
        return busNextDire;
    }

    public String getBusNextSName() {
        return busNextSName;
    }

    public String getBusNumPoint() {
        return busNumPoint;
    }

    public String getBusPointE() {
        return busPointE;
    }

    public String getBusPointN() {
        return busPointN;
    }
}
