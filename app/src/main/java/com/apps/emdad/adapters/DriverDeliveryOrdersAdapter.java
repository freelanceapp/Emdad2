package com.apps.emdad.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_Deliver_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_driver_order.Fragment_Driver_My_Order;
import com.apps.emdad.databinding.CurrentOrderRowBinding;
import com.apps.emdad.databinding.DriverDeliveryRowBinding;
import com.apps.emdad.databinding.LoadMoreRowBinding;
import com.apps.emdad.models.OrderModel;

import java.util.List;

import io.paperdb.Paper;

public class DriverDeliveryOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<OrderModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment fragment;
    private String lang;

    public DriverDeliveryOrdersAdapter(List<OrderModel> list, Context context, Fragment fragment) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment = fragment;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            DriverDeliveryRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.driver_delivery_row, parent, false);
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
            myHolder.binding.setLang(lang);
            String status = orderModel.getOrder_status();
            switch (status){
                case "new_order":
                case "have_offer":
                case "pennding":
                case "order_driver_back":
                case "client_cancel":
                case "cancel_for_late":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.GONE);
                    myHolder.binding.progBar.setProgress(0);

                    break;

                case "accept_driver":

                case "bill_attach":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.picking_order);
                    myHolder.binding.progBar.setProgress(1);

                    break;

                case "order_collected":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.delivering2);
                    myHolder.binding.progBar.setProgress(2);

                    break;
                case "reach_to_client":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.on_location);
                    myHolder.binding.progBar.setProgress(3);


                    break;

                case "driver_end_rate":
                case "client_end_and_rate":
                    myHolder.binding.llDistance.setVisibility(View.VISIBLE);
                    myHolder.binding.llOrderStatus.setVisibility(View.VISIBLE);
                    myHolder.binding.tvStatus.setText(R.string.delivered);
                    myHolder.binding.progBar.setProgress(4);

                    break;


            }

            myHolder.itemView.setOnClickListener(v -> {
                OrderModel orderModel1 = list.get(holder.getAdapterPosition());
                if (fragment instanceof Fragment_Driver_Deliver_Order){
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
        private DriverDeliveryRowBinding binding;

        public MyHolder(DriverDeliveryRowBinding binding) {
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
