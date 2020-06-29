package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetSideroadConnect {
    @SerializedName("nodeID")
    private int nodeID;
    @SerializedName("pointX")
    private String pointX;
    @SerializedName("pointY")
    private String pointY;
    @SerializedName("roadID")
    private int roadID;

    public int getNodeID() {
        return nodeID;
    }

    public String getPointX() {
        return pointX;
    }

    public String getPointY() {
        return pointY;
    }

    public int getRoadID() {
        return roadID;
    }
}
