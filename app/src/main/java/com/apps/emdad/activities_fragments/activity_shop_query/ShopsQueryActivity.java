package com.apps.emdad.activities_fragments.activity_shop_query;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_filter.FilterActivity;
import com.apps.emdad.activities_fragments.activity_map_search.MapSearchActivity;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.activities_fragments.activity_shop_map.ShopMapActivity;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.adapters.NearbyAdapter;
import com.apps.emdad.adapters.NearbyAdapter3;
import com.apps.emdad.adapters.ResentSearchAdapter;
import com.apps.emdad.databinding.ActivityShopsBinding;
import com.apps.emdad.databinding.ActivityShopsQueryBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CategoryModel;
import com.apps.emdad.models.DefaultSettings;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopsQueryActivity extends AppCompatActivity {
    private ActivityShopsQueryBinding binding;
    private List<NearbyModel.Result> resultList;
    private NearbyAdapter3 adapter;
    private double user_lat;
    private double user_lng;
    private SkeletonScreen skeletonScreen;
    private String lang;
    private boolean hasManyPages = false;
    private boolean isLoading = false;
    private String query = "";
    private String next_page="";
    private CategoryModel categoryModel;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_shops_query);
        getDataFromIntent();
        initView();
    }

    private void initView() {
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
        adapter = new NearbyAdapter3(resultList,this,user_lat,user_lng);
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
                    if (hasManyPages&&totalItem>=20&&(totalItem-pos==2)&&!isLoading){
                        isLoading = true;
                        loadMore();
                    }
                }
            }
        });


        binding.close.setOnClickListener(v -> super.onBackPressed());
        getShops(query);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat",0.0);
        user_lng = intent.getDoubleExtra("lng",0.0);
        categoryModel = (CategoryModel) intent.getSerializableExtra("data");

        query = categoryModel.getKeyword_google();
    }


    private void getShops(String query) {
        resultList.clear();
        adapter.notifyDataSetChanged();
        binding.tvNoData.setVisibility(View.GONE);
        skeletonScreen.show();

        String loc = user_lat+","+user_lng;
        Log.e("query",query);
        Log.e("loc2",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc,query,"distance",lang,"",getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        skeletonScreen.hide();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getStatus().equals("OK")){

                                if (response.body().getNext_page_token()!=null){
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                }else {
                                    hasManyPages = false;
                                    next_page = "";
                                }

                                if (response.body().getResults().size()>0)
                                {
                                    calculateDistance(response.body().getResults());
                                    binding.tvNoData.setVisibility(View.GONE);

                                }else
                                {
                                    binding.tvNoData.setVisibility(View.VISIBLE);

                                }
                            }else {
                                binding.tvNoData.setVisibility(View.VISIBLE);

                            }

                        }else
                        {

                            skeletonScreen.hide();

                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {
                            Log.e("Error",t.getMessage());
                            skeletonScreen.hide();
                            Toast.makeText(ShopsQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void loadMore() {

        resultList.add(null);
        adapter.notifyItemInserted(resultList.size()-1);


        String loc = user_lat+","+user_lng;
        Log.e("loc",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc,query,"distance",lang,next_page,getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        isLoading = false;
                        resultList.remove(resultList.size()-1);
                        adapter.notifyItemRemoved(resultList.size()-1);

                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            if (response.body().getStatus().equals("OK")){

                                if (response.body().getNext_page_token()!=null){
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                }else {
                                    hasManyPages = false;
                                    next_page = "";
                                }
                                if (response.body().getResults().size()>0)
                                {

                                    calculateDistanceLoadMore(response.body().getResults());
                                }
                            }

                        }else
                        {
                            isLoading = false;


                            try {
                                Log.e("error_code",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {
                            isLoading = false;
                            if (resultList.get(resultList.size()-1)==null){
                                resultList.remove(resultList.size()-1);
                                adapter.notifyItemRemoved(resultList.size()-1);
                            }
                            Log.e("Error",t.getMessage());
                            Toast.makeText(ShopsQueryActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {

                        }
                    }
                });




    }

    private void calculateDistance(List<NearbyModel.Result> results){
        List<NearbyModel.Result> resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<results.size();i++){
            NearbyModel.Result result = results.get(i);

            if (result!=null){


                result.setDistance(getDistance(new LatLng(user_lat,user_lng),new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng())));
                resultListFiltered.add(result);
            }

        }
        resultList.clear();
        adapter.notifyDataSetChanged();
        resultList.addAll(resultListFiltered);
        adapter.notifyDataSetChanged();

        if (resultList.size()>0){
            sortData();

        }else {
            binding.tvNoData.setVisibility(View.VISIBLE);

        }

    }

    private void calculateDistanceLoadMore(List<NearbyModel.Result> results){

        List<NearbyModel.Result> resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<results.size();i++){
            NearbyModel.Result result = results.get(i);

            if (result!=null){


                result.setDistance(getDistance(new LatLng(user_lat,user_lng),new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng())));
                resultListFiltered.add(result);
            }

        }


        int oldPos = resultList.size();
        resultList.addAll(resultListFiltered);

        int newPos = resultList.size();
        adapter.notifyItemRangeChanged(oldPos,newPos);


        sortData();

    }

    private void sortData(){
        Collections.sort(resultList, (o1, o2) -> {



            if (o1!=null&&o2!=null){
                if (o1.getDistance()<o2.getDistance()){
                    return -1;
                }else if (o1.getDistance()>o2.getDistance()){
                    return 1;
                }else{
                    return 0;

                }
            }else {
                return 0;
            }

        });

        adapter.notifyDataSetChanged();


    }

    private double getDistance(LatLng latLng1,LatLng latLng2){
        return SphericalUtil.computeDistanceBetween(latLng1,latLng2)/1000;
    }

    public void setShopData(NearbyModel.Result placeModel) {
        if (isRestaurant(placeModel)){
            //if has menu image or products

            Intent intent = new Intent(this, ShopDetailsActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);


        }else {
            Intent intent = new Intent(this, ShopMapActivity.class);
            intent.putExtra("data",placeModel);
            startActivity(intent);
        }

    }


    private boolean isRestaurant(NearbyModel.Result result){

        for (String type :result.getTypes()){
            if (type.equals("restaurant")){
                return true;
            }
        }

        return false;
    }

}