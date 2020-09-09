package com.apps.emdad.adapters.shop_products_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_shop_products.ShopProductActivity;
import com.apps.emdad.databinding.GroupTitleRowBinding;
import com.apps.emdad.databinding.ProductChildRowBinding;
import com.apps.emdad.models.ProductModel;
import com.apps.emdad.models.ShopDepartments;

import java.util.List;

import io.paperdb.Paper;

public class ProductAdapter extends RecyclerView.Adapter<ShopChildViewHolder> {
    private Context context;
    private String currency;
    private List<ProductModel> list;
    private String lang;
    private ShopProductActivity activity;
    private int parentPos = 0;

    public ProductAdapter(Context context, String currency, List<ProductModel> list,int parentPos) {
        this.context = context;
        this.currency = currency;
        this.list = list;
        this.parentPos = parentPos;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
        activity = (ShopProductActivity) context;

    }

    @NonNull
    @Override
    public ShopChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ProductChildRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),R.layout.product_child_row,parent,false);
        return new ShopChildViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopChildViewHolder holder, int position) {
        ProductModel productModel = list.get(position);
        holder.binding.setCurrency(currency);
        holder.binding.setModel(productModel);
        holder.itemView.setOnClickListener(v -> {
            ProductModel model = list.get(holder.getAdapterPosition());

            activity.setProductData(model,holder.getAdapterPosition(),parentPos);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
