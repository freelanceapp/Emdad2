package com.apps.emdad.adapters.shop_products_adapters;

import com.apps.emdad.models.ProductModel;
import com.apps.emdad.models.ShopDepartments;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ShopExpandGroup extends ExpandableGroup{
    private ShopDepartments shopDepartments;
    public ShopExpandGroup(ShopDepartments shopDepartments, List<ProductModel> items,String title) {
        super(title,items);
        this.shopDepartments = shopDepartments;

    }

    public ShopDepartments getShopDepartments() {
        return shopDepartments;
    }
}
