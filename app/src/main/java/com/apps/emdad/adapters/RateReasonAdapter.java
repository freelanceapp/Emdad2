package com.apps.emdad.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_chat.ChatActivity;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Main;
import com.apps.emdad.databinding.CategoryRowBinding;
import com.apps.emdad.databinding.RateReasonRowBinding;
import com.apps.emdad.models.CategoryModel;
import com.apps.emdad.models.RateReason;
import com.apps.emdad.tags.Tags;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.paperdb.Paper;

public class RateReasonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RateReason> list;
    private Context context;
    private LayoutInflater inflater;
    private ChatActivity activity;
    private int selectedPos = -1;


    public RateReasonAdapter(List<RateReason> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        activity = (ChatActivity) context;


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        RateReasonRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.rate_reason_row, parent, false);
        return new MyHolder(binding);


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        RateReason rateReason = list.get(position);
        myHolder.binding.setModel(rateReason);
        if (rateReason.isSelected()){
            myHolder.binding.fl.setBackgroundResource(R.drawable.small_rounded_primary);
            myHolder.binding.tvTitle.setTextColor(ContextCompat.getColor(context,R.color.white));
        }else {
            myHolder.binding.fl.setBackgroundResource(R.drawable.small_stroke_white);
            myHolder.binding.tvTitle.setTextColor(ContextCompat.getColor(context,R.color.black));
        }

        myHolder.itemView.setOnClickListener(v -> {
            if (selectedPos!=-1){
                RateReason reason = list.get(selectedPos);
                reason.setSelected(false);
                list.set(selectedPos,reason);
                notifyItemChanged(selectedPos);
            }
            selectedPos = myHolder.getAdapterPosition();
            RateReason reason = list.get(myHolder.getAdapterPosition());
            list.set(myHolder.getAdapterPosition(),reason);
            reason.setSelected(true);
            notifyItemChanged(myHolder.getAdapterPosition());

        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public RateReasonRowBinding binding;

        public MyHolder(@NonNull RateReasonRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


    public void addData(List<RateReason> rateReasonList){
        selectedPos = -1;
        list.clear();
        list.addAll(rateReasonList);
        notifyDataSetChanged();
    }


}
