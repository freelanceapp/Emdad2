package com.apps.emdad.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Main;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.databinding.CategoryRowBinding;
import com.apps.emdad.databinding.RecentSearchRowBinding;
import com.apps.emdad.models.CategoryModel;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CategoryModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment_Main fragment_main;

    public CategoryAdapter(List<CategoryModel> list, Context context,Fragment_Main fragment_main) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment_main = fragment_main;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        CategoryRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.category_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        CategoryModel categoryModel = list.get(position);
        myHolder.binding.setModel(categoryModel);
        Picasso.get().load(categoryModel.getImage()).fit().into(myHolder.binding.image);
        myHolder.itemView.setOnClickListener(v -> {
            CategoryModel categoryModel2 = list.get(holder.getAdapterPosition());

            fragment_main.setCategoryData(categoryModel2);
        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public CategoryRowBinding binding;

        public MyHolder(@NonNull CategoryRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }




}
