package com.apps.emdad.models;

import java.io.Serializable;
import java.util.List;

public class CreateOrderModel implements Serializable {
    private FavoriteLocationModel fromLocation;
    private FavoriteLocationModel toLocation;
    private String payment;
    private String order_details;
    private List<String> images;
    private String notes;
    private String time;
    private Coupon coupon;


}
