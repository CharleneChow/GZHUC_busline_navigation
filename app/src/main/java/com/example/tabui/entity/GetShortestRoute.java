package com.example.tabui.entity;

import com.google.gson.annotations.SerializedName;

public class GetShortestRoute {
    @SerializedName("id")
    private int id;
    @SerializedName("crossId")
    private String crossId;
    @SerializedName("move")
    private String move;
    @SerializedName("roadId")
    private String roadId;
    @SerializedName("nextCrossId")
    private String nextCrossId;
    @SerializedName("length")
    private String length;

    public int getId() {
        return id;
    }

    public String getCrossId() {
        return crossId;
    }

    public String getMove() {
        return move;
    }

    public String getRoadId() {
        return roadId;
    }

    public String getNextCrossId() {
        return nextCrossId;
    }

    public String getLength() {
        return length;
    }

}
