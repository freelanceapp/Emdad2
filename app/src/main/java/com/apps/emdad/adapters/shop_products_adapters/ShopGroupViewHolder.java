package com.apps.emdad.adapters.shop_products_adapters;

import android.view.View;

import com.apps.emdad.databinding.GroupTitleRowBinding;
import com.apps.emdad.databinding.ProductChildRowBinding;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class ShopGroupViewHolder extends GroupViewHolder {
    public GroupTitleRowBinding binding;
    public ShopGroupViewHolder(GroupTitleRowBinding binding) {
        super(binding.getRoot());
        this.binding =binding;
    }


}
