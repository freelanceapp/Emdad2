package com.apps.emdad.adapters.shop_products_adapters;

import com.apps.emdad.databinding.ProductChildRowBinding;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class ShopChildViewHolder extends ChildViewHolder {
    public ProductChildRowBinding binding;
    public ShopChildViewHolder(ProductChildRowBinding binding) {
        super(binding.getRoot());
        this.binding =binding;
    }
}
