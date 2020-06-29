package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetSideRoad {
    @SerializedName("edgeID")
    private int edgeID;
    @SerializedName("fnodeID")
    private int fnodeID;
    @SerializedName("tnodeID")
    private int tnodeID;
    @SerializedName("weight")
    private String weight;
    @SerializedName("grad")
    private int grad;

    public int getEdgeID() {
        return edgeID;
    }

    public int getFnodeID() {
        return fnodeID;
    }

    public int getTnodeID() {
        return tnodeID;
    }

    public String getWeight() {
        return weight;
    }

    public int getGrad() {
        return grad;
    }
}
