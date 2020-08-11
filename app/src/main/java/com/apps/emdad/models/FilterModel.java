package com.apps.emdad.models;

import java.io.Serializable;

public class FilterModel implements Serializable {
    private double distance;
    private double rate;
    private String keyword;
    public FilterModel() {
    }

    public FilterModel(double distance, double rate, String keyword) {
        this.distance = distance;
        this.rate = rate;
        this.keyword = keyword;
    }

    public double getDistance() {
        return distance;
    }

    public double getRate() {
        return rate;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
