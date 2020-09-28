package com.apps.emdad.activities_fragments.activity_delegate_orders;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_chat.ChatActivity;
import com.apps.emdad.activities_fragments.activity_delegate_add_offer.DelegateAddOfferActivity;
import com.apps.emdad.activities_fragments.activity_setting.SettingsActivity;
import com.apps.emdad.adapters.DriverComingOrdersAdapter;
import com.apps.emdad.adapters.OrdersAdapter;
import com.apps.emdad.databinding.ActivityDelegateOrdersBinding;
import com.apps.emdad.databinding.ActivityOldOrdersBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.OrderModel;
import com.apps.emdad.models.OrdersDataModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DelegateOrdersActivity extends AppCompatActivity {
    private ActivityDelegateOrdersBinding binding;
    private String lang;
    private DriverComingOrdersAdapter adapter;
    private UserModel userModel;
    private Preferences preferences;
    private int current_page = 1;
    private boolean isLoading = false;
    private List<OrderModel> orderModelList;
    private Call<OrdersDataModel> loadMoreCall;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delegate_orders);
        initView();
    }

    private void initView() {
        orderModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.llback.setOnClickListener(v -> finish());
        binding.setModel(userModel);

        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DriverComingOrdersAdapter(orderModelList, this);
        binding.recView.setAdapter(adapter);
        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();
                    int last_item_pos = manager.findLastCompletelyVisibleItemPosition();
                    int total_items_count = binding.recView.getAdapter().getItemCount();
                    if (last_item_pos == (total_items_count - 2) && !isLoading) {
                        int page = current_page + 1;
                        loadMore(page);
                    }
                }
            }
        });

        binding.btnAlert.setOnClickListener(v -> {
            if (userModel!=null){
                if (userModel.getUser().getReceive_notifications().equals("yes")){
                    updateReceiveNotification("no");
                }
            }
        });

        binding.btnEnableAlerts.setOnClickListener(v -> {

            if (userModel!=null){
                if (userModel.getUser().getReceive_notifications().equals("no")){
                    updateReceiveNotification("yes");
                }
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::getOrders);

        getOrders();
    }



    private void getOrders() {
        if (loadMoreCall != null) {
            loadMoreCall.cancel();
            if (orderModelList.size() > 0 && orderModelList.get(orderModelList.size() - 1) == null) {
                orderModelList.remove(orderModelList.size() - 1);
                adapter.notifyItemRemoved(orderModelList.size() - 1);
            }
        }
        Api.getService(Tags.base_url).getDriverComingOrder(userModel.getUser().getToken(), userModel.getUser().getId(), 1, "on", 20)
                .enqueue(new Callback<OrdersDataModel>() {
                    @Override
                    public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                        binding.prgBar.setVisibility(View.GONE);
                        binding.swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                                if (response.body().getData().size() > 0) {
                                    binding.llNoOrder.setVisibility(View.GONE);
                                    updateDataDistance(response.body().getData(),false);
                                    current_page = response.body().getCurrent_page();
                                } else {
                                    binding.llNoOrder.setVisibility(View.VISIBLE);

                                }


                            }
                        } else {
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<OrdersDataModel> call, Throwable t) {
                        binding.prgBar.setVisibility(View.GONE);
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(DelegateOrdersActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(DelegateOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });

    }

    private void loadMore(int page) {
        orderModelList.add(null);
        adapter.notifyItemInserted(orderModelList.size() - 1);
        isLoading = true;

        loadMoreCall = Api.getService(Tags.base_url).getDriverComingOrder(userModel.getUser().getToken(), userModel.getUser().getId(), page, "on", 20);
        loadMoreCall.enqueue(new Callback<OrdersDataModel>() {
            @Override
            public void onResponse(Call<OrdersDataModel> call, Response<OrdersDataModel> response) {
                isLoading = false;
                if (orderModelList.get(orderModelList.size() - 1) == null) {
                    orderModelList.remove(orderModelList.size() - 1);
                    adapter.notifyItemRemoved(orderModelList.size() - 1);
                }
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().getData().size() > 0) {
                        current_page = response.body().getCurrent_page();
                        updateDataDistance(response.body().getData(),true);


                    }
                } else {
                    isLoading = false;
                    if (orderModelList.get(orderModelList.size() - 1) == null) {
                        orderModelList.remove(orderModelList.size() - 1);
                        adapter.notifyItemRemoved(orderModelList.size() - 1);
                    }
                    try {
                        Log.e("error_code", response.code() + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<OrdersDataModel> call, Throwable t) {
                isLoading = false;
                if (orderModelList.get(orderModelList.size() - 1) == null) {
                    orderModelList.remove(orderModelList.size() - 1);
                    adapter.notifyItemRemoved(orderModelList.size() - 1);
                }
                try {
                    if (t.getMessage() != null) {
                        Log.e("error", t.getMessage() + "__");

                        if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                            Toast.makeText(DelegateOrdersActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                        } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                        } else {
                            Toast.makeText(DelegateOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                        }
                    }


                } catch (Exception e) {

                }
            }
        });
    }


    private void updateDataDistance(List<OrderModel> data, boolean isLoadMore) {
        LatLng user_location = new LatLng(Double.parseDouble(userModel.getUser().getLatitude()),Double.parseDouble(userModel.getUser().getLongitude()));
        for (int index = 0; index < data.size(); index++) {
            OrderModel orderModel = data.get(index);
            orderModel.setPick_up_distance(calculateDistance(user_location, new LatLng(Double.parseDouble(orderModel.getMarket_latitude()), Double.parseDouble(orderModel.getMarket_longitude()))));
            orderModel.setDrop_off_distance(calculateDistance(user_location, new LatLng(Double.parseDouble(orderModel.getClient_latitude()), Double.parseDouble(orderModel.getClient_longitude()))));

            data.set(index, orderModel);
        }

        if (!isLoadMore) {
            orderModelList.clear();
            orderModelList.addAll(data);
            adapter.notifyDataSetChanged();
        } else {
            int old_pos = orderModelList.size() - 1;
            orderModelList.addAll(data);
            int new_pos = orderModelList.size();
            adapter.notifyItemRangeInserted(old_pos, new_pos);
        }
    }

    private String calculateDistance(LatLng latLng1, LatLng latLng2) {
        return String.format(Locale.ENGLISH, "%s %s", String.format(Locale.ENGLISH, "%.2f", (SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000)), getString(R.string.km));

    }

    private void updateReceiveNotification(String state) {
        Api.getService(Tags.base_url)
                .updateReceiveNotification(userModel.getUser().getToken(),userModel.getUser().getId(),state)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        if (response.isSuccessful()) {

                            userModel = response.body();
                            binding.setModel(userModel);
                            preferences.create_update_userdata(DelegateOrdersActivity.this,userModel);
                        } else {
                            try {
                                Log.e("error", response.code() + "__" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (response.code() == 500) {
                                Toast.makeText(DelegateOrdersActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DelegateOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(DelegateOrdersActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DelegateOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage() + "__");
                        }
                    }
                });
    }

    public void setItemData(OrderModel orderModel1) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("order_id",orderModel1.getId());
        startActivity(intent);
        finish();
    }
}