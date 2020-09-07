package com.apps.emdad.models;

import java.io.Serializable;

public class ShopDepartments implements Serializable {
    private int id;
    private String market_id;
    private String title_ar;
    private String title_en;
    private String image;

    public int getId() {
        return id;
    }

    public String getMarket_id() {
        return market_id;
    }

    public String getTitle_ar() {
        return title_ar;
    }

    public String getTitle_en() {
        return title_en;
    }

    public String getImage() {
        return image;
    }
}
