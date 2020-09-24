package com.apps.emdad.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_Deliver_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_My_Order;
import com.apps.emdad.databinding.CurrentOrderRowBinding;
import com.apps.emdad.databinding.LoadMoreRowBinding;
import com.apps.emdad.models.OrderModel;

import java.util.List;

public class DriverOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<OrderModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;

    public DriverOrdersAdapter(List<OrderModel> list, Context context, Fragment fragment) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            CurrentOrderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.current_order_row, parent, false);
            return new MyHolder(binding);
        }else {
            LoadMoreRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.load_more_row, parent, false);
            return new LoadMoreHolder(binding);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            OrderModel orderModel = list.get(position);
            myHolder.binding.setModel(orderModel);

            if (orderModel.getOrder_status().equals("new_order")){
                myHolder.binding.icon.setImageResource(R.drawable.ic_clock2);
                myHolder.binding.icon.setColorFilter(ContextCompat.getColor(context,R.color.rate_color));
                myHolder.binding.tvState.setText(context.getString(R.string.pending));
            }else if (orderModel.getOrder_status().equals("client_end_and_rate")||orderModel.getOrder_status().equals("driver_end_rate")){
                myHolder.binding.icon.setImageResource(R.drawable.ic_checked);
                myHolder.binding.icon.setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary));
                myHolder.binding.tvState.setText(context.getString(R.string.done));
            }else if (orderModel.getOrder_status().equals("client_cancel")){
                myHolder.binding.icon.setImageResource(R.drawable.ic_error);
                myHolder.binding.icon.setColorFilter(ContextCompat.getColor(context,R.color.color_red));
                myHolder.binding.tvState.setText(context.getString(R.string.cancel));
            }

            myHolder.itemView.setOnClickListener(v -> {
                OrderModel orderModel1 = list.get(holder.getAdapterPosition());

                if (fragment instanceof Fragment_Driver_My_Order){
                    Fragment_Driver_My_Order fragment_driver_my_order = (Fragment_Driver_My_Order) fragment;
                    fragment_driver_my_order.setItemData(orderModel1);
                }else if (fragment instanceof Fragment_Driver_Deliver_Order){
                    Log.e("11","111");
                    Fragment_Driver_Deliver_Order fragment_driver_deliver_order = (Fragment_Driver_Deliver_Order) fragment;
                    fragment_driver_deliver_order.setItemData(orderModel1);

                }

            });

        }else if (holder instanceof LoadMoreHolder){
            LoadMoreHolder loadMoreHolder = (LoadMoreHolder) holder;
            loadMoreHolder.binding.prgBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            loadMoreHolder.binding.prgBar.setIndeterminate(true);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private CurrentOrderRowBinding binding;

        public MyHolder(CurrentOrderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        private LoadMoreRowBinding binding;

        public LoadMoreHolder(LoadMoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


        }

    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position)==null){
            return LOAD;
        }else {
            return DATA;
        }
    }
}
