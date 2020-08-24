package com.apps.emdad.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Main;
import com.apps.emdad.databinding.MainCategoryDataRowBinding;
import com.apps.emdad.databinding.MainSliderRowBinding;
import com.apps.emdad.models.CategoryModel;
import com.apps.emdad.models.MainItemData;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.remote.Api;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int SLIDER = 1;
    private final int DATA = 2;

    private List<MainItemData> list;
    private Context context;
    private LayoutInflater inflater;
    private Fragment_Main fragment_main;
    private List<NearbyModel.Result> resultList;
    private boolean hasManyPages = false;
    private boolean isLoading = false;
    private String query = "food|restaurant|supermarket|bakery";
    private String next_page = "";
    private List<Integer> sliderList;
    private SliderAdapter sliderAdapter;
    private SkeletonScreen skeletonPopular;
    private double user_lat=0.0,user_lng=0.0;
    private NearbyAdapter2 nearbyAdapter;
    private CategoryAdapter categoryAdapter;
    private List<CategoryModel> categoryModelList;
    private HomeActivity activity;
    private String lang;
    private Timer timer;
    private Task task;
    public MainAdapter(List<MainItemData> list, Context context, Fragment_Main fragment_main, double user_lat, double user_lng) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.fragment_main = fragment_main;
        resultList = new ArrayList<>();
        sliderList = new ArrayList<>();
        categoryModelList = new ArrayList<>();
        this.user_lat = user_lat;
        this.user_lng = user_lng;
        Paper.init(context);
        lang = Paper.book().read("lang","ar");
        activity = (HomeActivity) context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType==DATA){
            MainCategoryDataRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.main_category_data_row, parent, false);
            return new MyHolder(binding);
        }else {
            MainSliderRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.main_slider_row, parent, false);
            return new SliderHolder(binding);
        }



    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof MyHolder){
            MyHolder myHolder = (MyHolder) holder;
            myHolder.binding.recView.setLayoutManager(new GridLayoutManager(context,2));
            categoryAdapter = new CategoryAdapter(categoryModelList,context);
            myHolder.binding.recView.setAdapter(categoryAdapter);
            getCategory();

        }else if (holder instanceof SliderHolder){
            SliderHolder sliderHolder = (SliderHolder) holder;

            sliderAdapter = new SliderAdapter(sliderList, context);
            sliderHolder.binding.pager.setAdapter(sliderAdapter);

            sliderHolder.binding.recViewPopular.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));

            sliderHolder.binding.recViewPopular.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dx>0){
                        int totalItem = nearbyAdapter.getItemCount();
                        LinearLayoutManager manager = (LinearLayoutManager) sliderHolder.binding.recViewPopular.getLayoutManager();
                        int pos = manager.findLastCompletelyVisibleItemPosition();
                        if (hasManyPages&&totalItem>=18&&(totalItem-pos==2)&&!isLoading){
                            isLoading = true;
                            loadMore();

                        }
                    }
                }
            });



            skeletonPopular = Skeleton.bind(sliderHolder.binding.recViewPopular)
                    .frozen(false)
                    .duration(1000)
                    .shimmer(true)
                    .count(2)
                    .load(R.layout.shop_row)
                    .show();

            addSliderImages(sliderHolder.binding);
            getNearByShops(sliderHolder.binding);


        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void stopTimer() {
        if (timer!=null){
            timer.purge();
            timer.cancel();
        }
        if (task!=null){
            task.cancel();
        }
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        public MainCategoryDataRowBinding binding;

        public MyHolder(@NonNull MainCategoryDataRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }

    public static class SliderHolder extends RecyclerView.ViewHolder {
        public MainSliderRowBinding binding;

        public SliderHolder(@NonNull MainSliderRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }


    @Override
    public int getItemViewType(int position) {
        MainItemData itemData = list.get(position);
        if (itemData.getType()==0){
            return SLIDER;
        }else {
            return DATA;
        }







    }



    private void addSliderImages(MainSliderRowBinding binding) {
        sliderList.add(R.drawable.img1);
        sliderList.add(R.drawable.img2);
        sliderList.add(R.drawable.img1);
        sliderList.add(R.drawable.img2);
        sliderAdapter.notifyDataSetChanged();
        updateSliderUi(binding);
    }

    private void updateSliderUi(MainSliderRowBinding binding) {
        if (sliderList.size() > 0) {
            int margin = (int) (context.getResources().getDisplayMetrics().density * 10);
            int padding = (int) (context.getResources().getDisplayMetrics().density * 40);
            binding.pager.setPageMargin(margin);
            binding.pager.setPadding(padding, 0, padding, 0);

            if (sliderList.size() > 1) {
                timer = new Timer();
                task = new Task(binding);
                timer.scheduleAtFixedRate(task, 3000, 3000);
            }
        }
    }

    private void getNearByShops(MainSliderRowBinding binding) {
        String loc = user_lat + "," + user_lng;
        Log.e("loc",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc, query, "distance", lang, "", context.getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        skeletonPopular.hide();
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().getStatus().equals("OK")) {

                                if (response.body().getNext_page_token() != null) {
                                    hasManyPages = true;
                                    next_page = response.body().getNext_page_token();
                                } else {
                                    hasManyPages = false;
                                    next_page = "";
                                }

                                if (response.body().getResults().size() > 0) {
                                    calculateDistance(response.body().getResults(),binding);

                                } else {
                                    binding.llPopular.setVisibility(View.GONE);

                                }
                            } else {
                                binding.llPopular.setVisibility(View.GONE);

                            }

                        } else {

                            skeletonPopular.hide();

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<NearbyModel> call, Throwable t) {
                        try {
                            Log.e("Error", t.getMessage());
                            skeletonPopular.hide();
                            Toast.makeText(context, context.getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void loadMore() {

        resultList.add(null);
        nearbyAdapter.notifyItemInserted(resultList.size()-1);


        String loc = user_lat+","+user_lng;
        Log.e("loc",loc);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .nearbyPlaceRankBy(loc,query,"distance",lang,next_page,context.getString(R.string.map_api_key))
                .enqueue(new Callback<NearbyModel>() {
                    @Override
                    public void onResponse(Call<NearbyModel> call, Response<NearbyModel> response) {
                        isLoading = false;
                        resultList.remove(resultList.size()-1);
                        nearbyAdapter.notifyItemRemoved(resultList.size()-1);

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
                                nearbyAdapter.notifyItemRemoved(resultList.size()-1);
                            }
                            Log.e("Error",t.getMessage());
                            Toast.makeText(context, context.getString(R.string.something), Toast.LENGTH_LONG).show();
                        }catch (Exception e)
                        {

                        }
                    }
                });
    }

    private void calculateDistance(List<NearbyModel.Result> results,MainSliderRowBinding binding){
        for (int i =0 ;i<results.size();i++){
            NearbyModel.Result result = results.get(i);

            if (result!=null){

                result.setDistance(getDistance(new LatLng(user_lat,user_lng),new LatLng(result.getGeometry().getLocation().getLat(),result.getGeometry().getLocation().getLng())));
                resultList.add(result);
            }

        }

        Log.e("size",resultList.size()+"__");

        nearbyAdapter = new NearbyAdapter2(resultList,context,fragment_main);
        binding.recViewPopular.setAdapter(nearbyAdapter);



    }

    private void calculateDistanceLoadMore(List<NearbyModel.Result> results) {

        List<NearbyModel.Result> resultListFiltered = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            NearbyModel.Result result = results.get(i);

            if (result != null) {


                result.setDistance(getDistance(new LatLng(user_lat, user_lng), new LatLng(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng())));
                resultListFiltered.add(result);

            }

        }

        int oldPos = resultList.size()-1;

        resultList.addAll(resultListFiltered);
        int newPos = resultList.size();
        nearbyAdapter.notifyItemRangeChanged(oldPos,newPos);


    }

    private double getDistance(LatLng latLng1, LatLng latLng2) {
        return SphericalUtil.computeDistanceBetween(latLng1, latLng2) / 1000;
    }


    private void getCategory() {
        CategoryModel model1 = new CategoryModel("1",R.drawable.rest,context.getString(R.string.restaurants));
        CategoryModel model2 = new CategoryModel("2",R.drawable.sup,context.getString(R.string.supermarket));
        CategoryModel model3 = new CategoryModel("3",R.drawable.cafe,context.getString(R.string.cafe));
        CategoryModel model4 = new CategoryModel("4",R.drawable.bakery,context.getString(R.string.bakery));
        CategoryModel model5 = new CategoryModel("5",R.drawable.store,context.getString(R.string.stores));
        CategoryModel model6 = new CategoryModel("6",R.drawable.gift,context.getString(R.string.florist));
        CategoryModel model7 = new CategoryModel("7",R.drawable.lib,context.getString(R.string.library));
        CategoryModel model8 = new CategoryModel("8",R.drawable.pharm,context.getString(R.string.pharmacy));

        categoryModelList.add(model1);
        categoryModelList.add(model2);
        categoryModelList.add(model3);
        categoryModelList.add(model4);
        categoryModelList.add(model5);
        categoryModelList.add(model6);
        categoryModelList.add(model7);
        categoryModelList.add(model8);

        categoryAdapter.notifyDataSetChanged();


    }




    private  class Task extends TimerTask{
        private MainSliderRowBinding binding;

        public Task(MainSliderRowBinding binding) {
            this.binding = binding;
        }

        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (binding.pager.getCurrentItem()<sliderList.size()-1){
                        binding.pager.setCurrentItem(binding.pager.getCurrentItem()+1);
                    }else {
                        binding.pager.setCurrentItem(0,false);
                    }
                }
            });
        }
    }
}
