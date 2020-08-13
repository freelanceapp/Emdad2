package com.apps.emdad.activities_fragments.activity_shops;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.apps.emdad.adapters.NearbyAdapter;
import com.apps.emdad.databinding.ActivityShopsBinding;
import com.apps.emdad.interfaces.Listeners;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.FilterModel;
import com.apps.emdad.models.NearbyModel;
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

public class ShopsActivity extends AppCompatActivity implements Listeners.BackListener {
    private ActivityShopsBinding binding;
    private List<NearbyModel.Result> resultList;
    private NearbyAdapter adapter;
    private double user_lat;
    private double user_lng;
    private SkeletonScreen skeletonScreen;
    private String lang;
    private boolean hasManyPages = false;
    private boolean isLoading = false;
    private String query = "restaurant|food|supermarket|bakery";
    private String next_page="";
    private double rate = 5.0;
    private int distance = 60000;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_shops);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        user_lat = intent.getDoubleExtra("lat",0.0);
        user_lng = intent.getDoubleExtra("lng",0.0);

    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setCount(0);
        binding.setQuery("");
        binding.setListener(this);

        resultList = new ArrayList<>();
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NearbyAdapter(resultList,this,user_lat,user_lng);
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
                        if (query.equals("restaurant|food|supermarket|bakery")&&rate==5.0&&distance==60000){
                            loadMore();

                        }else {
                            loadMoreSearch();
                        }
                    }
                }
            }
        });

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId== EditorInfo.IME_ACTION_SEARCH){
                query = binding.edtSearch.getText().toString().trim();
                if (!query.isEmpty()){

                    search(query,distance,rate);
                }
            }
            return false;
        });

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()){
                    clear();
                }else {
                    binding.tvCancel.setVisibility(View.VISIBLE);
                }
            }
        });
        binding.tvCancel.setOnClickListener(v -> {
            clear();
            binding.edtSearch.setText(null);
        });

        binding.imageFilter.setOnClickListener(v -> {
            Intent intent = new Intent(this, FilterActivity.class);
            startActivityForResult(intent,100);
        });

        getShops(query);
    }

    private void clear() {
        rate = 5.0;
        distance = 60000;
        next_page="";
        binding.tvCancel.setVisibility(View.GONE);
        query = "restaurant|food|supermarket|bakery";
        binding.setCount(0);
        binding.setQuery("");
        getShops(query);
    }

    private void getShops(String query) {
        resultList.clear();
        adapter.notifyDataSetChanged();
        binding.tvNoData.setVisibility(View.GONE);
        skeletonScreen.show();

        String loc = user_lat+","+user_lng;
        Log.e("loc",loc);
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
                                    resultList.clear();
                                    resultList.addAll(response.body().getResults());
                                    adapter.notifyDataSetChanged();
                                    binding.setCount(resultList.size());
                                    calculateDistance(rate);


                                    binding.tvNoData.setVisibility(View.GONE);

                                }else
                                {
                                    binding.tvNoData.setVisibility(View.VISIBLE);
                                    binding.setCount(0);

                                }
                            }else {
                                binding.tvNoData.setVisibility(View.VISIBLE);
                                binding.setCount(0);

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
                            binding.setCount(0);
                            Log.e("Error",t.getMessage());
                            skeletonScreen.hide();
                            Toast.makeText(ShopsActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
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
                                    int oldPos = resultList.size();
                                    resultList.addAll(response.body().getResults());
                                    int newPos = resultList.size();
                                    binding.setCount(newPos);
                                    adapter.notifyItemRangeChanged(oldPos,newPos);
                                    calculateDistance(rate);
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
                            Toast.makeText(ShopsActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void search(String query,int distance,double rate) {
        Log.e("q",query+"__"+distance+"__"+rate);
        binding.tvNoData.setVisibility(View.GONE);
        resultList.clear();
        adapter.notifyDataSetChanged();
        skeletonScreen.show();
        if (query.equals("restaurant|food|supermarket|bakery")){
            binding.setQuery("");

        }else {
            binding.setQuery(query);

        }
        String loc = user_lat+","+user_lng;
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceInDistance(loc,query,distance,lang,"",getString(R.string.map_api_key))
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

                                    resultList.addAll(response.body().getResults());
                                    adapter.notifyDataSetChanged();
                                    calculateDistance(rate);
                                    binding.tvNoData.setVisibility(View.GONE);

                                }else
                                {
                                    binding.tvNoData.setVisibility(View.VISIBLE);
                                    binding.setCount(0);

                                }
                            }else {
                                binding.setCount(0);

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
                            binding.setCount(0);
                            Log.e("Error",t.getMessage());
                            skeletonScreen.hide();
                            Toast.makeText(ShopsActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void loadMoreSearch() {

        resultList.add(null);
        adapter.notifyItemInserted(resultList.size()-1);
        Log.e("q",query+"___"+distance);

        String loc = user_lat+","+user_lng;
        Log.e("loc",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceInDistance(loc,query,distance,lang,"",getString(R.string.map_api_key))
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

                                    resultList.addAll(response.body().getResults());
                                    adapter.notifyDataSetChanged();
                                    binding.setCount(resultList.size());
                                    calculateDistance(rate);
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
                            Toast.makeText(ShopsActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }





    private void calculateDistance(double rate){
        List<NearbyModel.Result> resultListFiltered = new ArrayList<>();

        for (int i =0 ;i<resultList.size();i++){
            NearbyModel.Result result = resultList.get(i);
            if (result!=null){
                if (result.getRating()<=rate){
                    result.setDistance(getDistance(new LatLng(user_lat,user_lng),new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng())));
                    resultListFiltered.add(result);
                }
            }

        }
        resultList.clear();
        adapter.notifyDataSetChanged();
        resultList.addAll(resultListFiltered);
        adapter.notifyDataSetChanged();
        binding.setCount(resultList.size());

        sortData();

    }

    private void sortData(){
        Collections.sort(resultList, (o1, o2) -> {
            if (o1.getDistance()<o2.getDistance()){
                return -1;
            }else if (o1.getDistance()>o2.getDistance()){
                return 1;
            }else{
                return 0;

            }
        });

        adapter.notifyDataSetChanged();


    }

    private double getDistance(LatLng latLng1,LatLng latLng2){
        return SphericalUtil.computeDistanceBetween(latLng1,latLng2)/1000;
    }

    public void setShopData(NearbyModel.Result placeModel) {
        Intent intent = getIntent();
        intent.putExtra("data",placeModel);
        setResult(RESULT_OK,intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100&&resultCode==RESULT_OK&&data!=null){
            binding.edtSearch.setText(null);
            FilterModel filterModel = (FilterModel) data.getSerializableExtra("data");
            if (!filterModel.getKeyword().isEmpty()){
                query = filterModel.getKeyword();
                binding.tvRecentSearch.setText(query);
            }else {

                binding.tvRecentSearch.setText(null);

            }
            rate  = filterModel.getRate();
            distance =  filterModel.getDistance()*1000;
            resultList.clear();
            adapter.notifyDataSetChanged();
            search(query,distance,rate);

        }
    }

    @Override
    public void back() {
        super.onBackPressed();
    }


}