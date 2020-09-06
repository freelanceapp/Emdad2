package com.apps.emdad.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.databinding.MenuImageRowBinding;
import com.apps.emdad.databinding.WorkHourRowBinding;
import com.apps.emdad.models.CustomPlaceModel;
import com.apps.emdad.models.HourModel;

import java.util.List;

public class MenuImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CustomPlaceModel.MenuImage> list;
    private Context context;
    private LayoutInflater inflater;
    private ShopDetailsActivity activity;

    public MenuImageAdapter(List<CustomPlaceModel.MenuImage> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (ShopDetailsActivity) context;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        MenuImageRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.menu_image_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        myHolder.binding.setModel(list.get(position));
        myHolder.itemView.setOnClickListener(v -> {
            CustomPlaceModel.MenuImage menuImage = list.get(myHolder.getAdapterPosition());
            activity.setMenuItem(menuImage,myHolder.binding.image);
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public MenuImageRowBinding binding;

        public MyHolder(@NonNull MenuImageRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
