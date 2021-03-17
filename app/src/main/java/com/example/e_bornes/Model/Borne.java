package com.example.e_bornes.Model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;
import java.util.Map;

public class Borne implements Serializable, ClusterItem {
    private String accessibility, access, address, zip, name;
    private int max_power;
    private double[] coordinates;

    public Borne(String[] params, int max_power, double[] coordinates) {
        accessibility = params[0];
        zip = params[1];
        access = params[2];
        address = params[3];
        name = params[4];

        this.max_power = max_power;
        this.coordinates = coordinates;
    }

    public String getName(){
        return name;
    }

    public String getAccessibility(){
        return accessibility;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(coordinates[0], coordinates[1]);
    }

    @Nullable
    @Override
    public String getTitle() {
        return name;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }
}
