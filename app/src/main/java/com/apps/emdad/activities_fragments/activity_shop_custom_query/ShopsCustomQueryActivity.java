package com.apps.emdad.activities_fragments.activity_shop_custom_query;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_custom_shop_details.CustomShopDetailsActivity;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.activities_fragments.activity_shop_map.ShopMapActivity;
import com.apps.emdad.activities_fragments.activity_shop_products.ShopProductActivity;
import com.apps.emdad.adapters.CustomShopsAdapter;
import com.apps.emdad.adapters.MainAdapter;
import com.apps.emdad.adapters.NearbyAdapter3;
import com.apps.emdad.adapters.SliderAdapter;
import com.apps.emdad.databinding.ActivityShopsCustomQueryBinding;
import com.apps.emdad.databinding.ActivityShopsQueryBinding;
import com.apps.emdad.databinding.MainSliderRowBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CategoryModel;
import com.apps.emdad.models.CustomPlaceDataModel;
import com.apps.emdad.models.CustomPlaceDataModel2;
import com.apps.emdad.models.CustomPlaceModel;
import com.apps.emdad.models.CustomShopDataModel;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.models.SliderModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopsCustomQueryActivity extends AppCompatActivity {
    private ActivityShopsCustomQueryBinding binding;
    private List<CustomPlaceModel> resultList;
    private CustomShopsAdapter adapter;
    private double user_lat;
    private double user_lng;
    private SkeletonScreen skeletonScreen;
    private String lang;
    private boolean isLoading = false;
    private int current_page = 1;
    private String department_id = "";
    private CategoryModel categoryModel;
    private Preferences preferences;
    private UserModel userModel;
    private List<SliderModel.Data> sliderList;
    private SliderAdapter sliderAdapter;
    private Timer timer;
    private int current_pages = 0, NUM_PAGES;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_shops_custom_query);
        getDataFromIntent();
        initView();
    }

    private void initView() {
        preferences = Preferences.getInstance();
        sliderList = new ArrayList<>();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        if (lang.equals("ar")){
            binding.setQuery(categoryModel.getTitle_ar());

        }else {
            binding.setQuery(categoryModel.getTitle_en());

        }
        resultList = new ArrayList<>();
        binding.recView.setLayoutManager(new LinearLayoutManager(this));

        String currency=getString(R.string.sar);
        if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }

        adapter = new CustomShopsAdapter(resultList,this,currency);
        binding.recView.setAdapter(adapter);



        skeletonScreen = Skeleton.bind(binding.recView)
                .adapter(adapter)
                .count(5)
                .frozen(false)
                .shimmer(true)
                .show();

        binding.recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy>0){
                    int totalItem = adapter.getItemCount();
                    LinearLayoutManager manager = (LinearLayoutManager) binding.recView.getLayoutManager();
                    int pos = manager.findLastCompletelyVisibleItemPosition();
                    if (totalItem>=20&&(totalItem-pos==2)&&!isLoading){
                        isLoading = true;
                        int page = current_page+1;
                        loadMore(page);
                    }
                }
            }
        });


        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this,R.color.colorPrimary),ContextCompat.getColor(this,R.color.color_red),ContextCompat.getColor(this,R.color.yellow),ContextCompat.getColor(this,R.color.color_blue));
        binding.close.setOnClickListener(v -> super.onBackPressed());
        binding.swipeRefresh.setOnRefreshListener(() -> getShops(categoryModel.getId()));
        getShops(categoryModel.getId());
        addSliderImages();
        change_slide_image();

    }
    private void change_slide_image() {
        final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (current_pages == NUM_PAGES) {
                    current_pages = 0;
                }
                binding.pager.setCurrentItem(current_pages++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 3000, 3000);
    }
    private void addSliderImages() {

        Api.getService(Tags.base_url)
                .getMarketSlider("market")
                .enqueue(new Callback<SliderModel>() {
                    @Override
                    public void onResponse(Call<SliderModel> call, Response<SliderModel> response) {
                        binding.progBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            if (response.body().getData().size() > 0) {
                                NUM_PAGES = response.body().getData().size();
                                Log.e("nnnnn",NUM_PAGES+"");
                                Log.e("nnnnn",response.body().getData().get(0).getImage()+"");

                                sliderAdapter = new SliderAdapter(response.body().getData(),ShopsCustomQueryActivity.this);
                                binding.pager.setAdapter(sliderAdapter);

                            } else {

                                binding.pager.setVisibility(View.GONE);
                            }
                        } else if (response.code() == 404) {
                            binding.pager.setVisibility(View.GONE);
                        } else {
                            binding.pager.setVisibility(View.GONE);
                            try {
                                Log.e("Error_code", response.code() + "_" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }

                    @Override
                    public void onFailure(Call<SliderModel> call, Throwable t) {
                        try {
                            Log.e("Error", t.getMessage());
                            binding.progBar.setVisibility(View.GONE);

                            Toast.makeText(ShopsCustomQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });

    }
    private void getDataFromIntent() {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat",0.0);
        user_lng = intent.getDoubleExtra("lng",0.0);
        categoryModel = (CategoryModel) intent.getSerializableExtra("data");

    }


    private void getShops(String department_id) {
        resultList.clear();
        adapter.notifyDataSetChanged();
        binding.tvNoData.setVisibility(View.GONE);
        skeletonScreen.show();

        Api.getService(Tags.base_url)
                .getCustomShops(department_id,1,"on",20)
                .enqueue(new Callback<CustomPlaceDataModel2>() {
                    @Override
                    public void onResponse(Call<CustomPlaceDataModel2> call, Response<CustomPlaceDataModel2> response) {
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getData().size()>0)
                            {
                                calculateDistance(response.body().getData());
                                binding.tvNoData.setVisibility(View.GONE);

                            }else
                            {
                                skeletonScreen.hide();
                                binding.tvNoData.setVisibility(View.VISIBLE);

                            }

                        }else
                        {
                            binding.swipeRefresh.setRefreshing(false);

                            skeletonScreen.hide();

                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<CustomPlaceDataModel2> call, Throwable t) {
                        try {
                            binding.swipeRefresh.setRefreshing(false);


                        if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ShopsCustomQueryActivity.this, R.string.something, Toast.LENGTH_SHORT).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }

                                else {
                                    Toast.makeText(ShopsCustomQueryActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            Log.e("Error",t.getMessage());
                            skeletonScreen.hide();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void loadMore(int page) {

        resultList.add(null);
        adapter.notifyItemInserted(resultList.size()-1);


        Api.getService(Tags.base_url)
                .getCustomShops(department_id,page,"on",20)
                .enqueue(new Callback<CustomPlaceDataModel2>() {
                    @Override
                    public void onResponse(Call<CustomPlaceDataModel2> call, Response<CustomPlaceDataModel2> response) {

                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getData().size()>0)
                            {
                                current_page = response.body().getCurrent_page();
                                calculateDistanceLoadMore(response.body().getData());
                            }else {
                                isLoading = false;
                                if (resultList.get(resultList.size()-1)==null){
                                    resultList.remove(resultList.size()-1);
                                    adapter.notifyItemRemoved(resultList.size()-1);
                                }
                            }

                        }else
                        {
                            isLoading = false;
                            if (resultList.get(resultList.size()-1)==null){
                                resultList.remove(resultList.size()-1);
                                adapter.notifyItemRemoved(resultList.size()-1);
                            }

                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<CustomPlaceDataModel2> call, Throwable t) {
                        try {
                            isLoading = false;
                            if (resultList.get(resultList.size()-1)==null){
                                resultList.remove(resultList.size()-1);
                                adapter.notifyItemRemoved(resultList.size()-1);
                            }
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(ShopsCustomQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }
                                else {
                                    Toast.makeText(ShopsCustomQueryActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {

                        }
                    }
                });




    }

    private void calculateDistance(List<CustomPlaceModel> results){
        List<CustomPlaceModel> resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<results.size();i++){
            CustomPlaceModel result = results.get(i);

            if (result!=null){



                result.setDistance(getDistance(new LatLng(user_lat,user_lng),new LatLng(Double.parseDouble(result.getLatitude()),Double.parseDouble(result.getLongitude()))));
                resultListFiltered.add(result);
            }

        }


        if (resultListFiltered.size()>0){
            binding.swipeRefresh.setRefreshing(false);

            skeletonScreen.hide();
            resultList.clear();
            resultList.addAll(resultListFiltered);
            adapter.notifyDataSetChanged();

        }else {
            binding.swipeRefresh.setRefreshing(false);

            skeletonScreen.hide();
            binding.tvNoData.setVisibility(View.VISIBLE);

        }

    }

    private void calculateDistanceLoadMore(List<CustomPlaceModel> results){


        List<CustomPlaceModel> resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<results.size();i++){
            CustomPlaceModel result = results.get(i);

            if (result!=null){


                result.setDistance(getDistance(new LatLng(user_lat,user_lng),new LatLng(Double.parseDouble(result.getLatitude()),Double.parseDouble(result.getLongitude()))));
                resultListFiltered.add(result);
            }

        }



        isLoading = false;
        if (resultList.get(resultList.size()-1)==null){
            resultList.remove(resultList.size()-1);
            adapter.notifyItemRemoved(resultList.size()-1);
        }
        int oldPos = resultList.size();
        resultList.addAll(results);

        int newPos = resultList.size();
        adapter.notifyItemRangeChanged(oldPos,newPos);

        //sortData();

    }


    private double getDistance(LatLng latLng1,LatLng latLng2){
        return SphericalUtil.computeDistanceBetween(latLng1,latLng2)/1000;
    }

    public void setShopData(CustomPlaceModel placeModel) {

        if (Integer.parseInt(placeModel.getProducts_count())>0){
            String max_Offer_value="";
            if (placeModel.getDelivery_offer()!=null){
                max_Offer_value = placeModel.getDelivery_offer().getLess_value();
            }
            CustomShopDataModel customShopDataModel = new CustomShopDataModel(placeModel.getGoogle_place_id(),placeModel.getId(),placeModel.getName(),placeModel.getAddress(),Double.parseDouble(placeModel.getLatitude()),Double.parseDouble(placeModel.getLongitude()),max_Offer_value,isOpen(placeModel),placeModel.getComments_count(),placeModel.getRating(),"custom",placeModel.getDelivery_offer(),null,placeModel.getDays());

            Intent intent = new Intent(this, ShopProductActivity.class);
            intent.putExtra("data",customShopDataModel);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, CustomShopDetailsActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);
        }



    }



    private boolean isOpen(CustomPlaceModel placeModel) {
        if (placeModel.getDays()!=null&&placeModel.getDays().size()>0&&placeModel.getDays().get(0).getStatus().equals("open")) {
            return true;
        }

        return false;
    }

}