package com.apps.emdad.adapters.shop_products_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.apps.emdad.R;
import com.apps.emdad.databinding.GroupTitleRowBinding;
import com.apps.emdad.databinding.ProductChildRowBinding;
import com.apps.emdad.models.ProductModel;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class ProductAdapter extends ExpandableRecyclerViewAdapter<ShopGroupViewHolder,ShopChildViewHolder> {
    private Context context;
    private String currency;
    public ProductAdapter(List<? extends ExpandableGroup> groups,Context context,String currency) {
        super(groups);
        this.context = context;
        this.currency = currency;
    }

    @Override
    public ShopGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        GroupTitleRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.group_title_row,parent,false);
        return new ShopGroupViewHolder(binding);
    }

    @Override
    public ShopChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        ProductChildRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.product_child_row,parent,false);
        return new ShopChildViewHolder(binding);

    }

    @Override
    public void onBindChildViewHolder(ShopChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        ProductModel productModel = (ProductModel) group.getItems().get(childIndex);
        holder.binding.setCurrency(currency);
        holder.binding.setModel(productModel);

    }

    @Override
    public void onBindGroupViewHolder(ShopGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.binding.setTitle(group.getTitle());

    }


    @Override
    public boolean toggleGroup(int flatPos) {
        return super.toggleGroup(flatPos);
    }
}
