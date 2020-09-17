package com.apps.emdad.models;

import java.io.Serializable;
import java.util.List;

public class SingleOrderDataModel implements Serializable {
    private OrderModel order;

    public OrderModel getOrder() {
        return order;
    }
}
