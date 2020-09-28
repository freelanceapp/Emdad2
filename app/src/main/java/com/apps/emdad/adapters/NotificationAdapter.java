package com.apps.emdad.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_chat.ChatActivity;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Notifications;
import com.apps.emdad.databinding.LoadMoreRowBinding;
import com.apps.emdad.databinding.NotificationRowBinding;
import com.apps.emdad.databinding.OfferRowBinding;
import com.apps.emdad.models.NotificationDataModel;
import com.apps.emdad.models.OffersModel;
import com.apps.emdad.share.Time_Ago;

import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int DATA = 1;
    private final int LOAD = 2;
    private List<NotificationDataModel.NotificationModel> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment_Notifications fragment_notifications;


    public NotificationAdapter(List<NotificationDataModel.NotificationModel> list, Context context, Fragment_Notifications fragment_notifications) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment_notifications = fragment_notifications;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            NotificationRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.notification_row, parent, false);
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
            NotificationDataModel.NotificationModel model = list.get(position);
            myHolder.binding.setModel(model);
            myHolder.binding.tvDate.setText(Time_Ago.getTimeAgo(Long.parseLong(model.getNotification_date())*1000,context));

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
        private NotificationRowBinding binding;

        public MyHolder(NotificationRowBinding binding) {
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
