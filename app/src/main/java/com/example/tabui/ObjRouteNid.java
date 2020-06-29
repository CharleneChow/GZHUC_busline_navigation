package com.example.tabui;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class ObjRouteNid {
    private int fromid;
    private int toid;
    private ArrayList<GeoPoint> displayL=new ArrayList<>();
    public int getFromid() {
        return fromid;
    }

    public void setFromid(int fromid) {
        this.fromid = fromid;
    }

    public int getToid() {
        return toid;
    }

    public void setToid(int toid) {
        this.toid = toid;
    }

    public ArrayList<GeoPoint> getDisplayL() {
        return displayL;
    }

    public void setDisplayL(ArrayList<GeoPoint> displayL) {
        this.displayL = displayL;
    }

}
