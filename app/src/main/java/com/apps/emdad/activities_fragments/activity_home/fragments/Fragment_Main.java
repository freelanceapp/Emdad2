package com.apps.emdad.activities_fragments.activity_home.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.activities_fragments.activity_shop_map.ShopMapActivity;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.adapters.MainAdapter;
import com.apps.emdad.databinding.FragmentMainBinding;
import com.apps.emdad.models.MainItemData;
import com.apps.emdad.models.NearbyModel;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class Fragment_Main extends Fragment {
    private final static String TAG1 = "lat";
    private final static String TAG2 = "lng";
    private double user_lat = 0.0, user_lng = 0.0;
    private HomeActivity activity;
    private FragmentMainBinding binding;
    private String lang;
    private List<MainItemData> mainItemDataList;
    private MainAdapter mainAdapter;


    public static Fragment_Main newInstance(double user_lat, double user_lng) {
        Bundle bundle = new Bundle();
        bundle.putDouble(TAG1, user_lat);
        bundle.putDouble(TAG2, user_lng);
        Fragment_Main fragment_main = new Fragment_Main();
        fragment_main.setArguments(bundle);
        return fragment_main;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        mainItemDataList = new ArrayList<>();

        activity = (HomeActivity) getActivity();
        Paper.init(activity);
        lang = Paper.book().read("lang", "ar");
        Bundle bundle = getArguments();
        if (bundle != null) {
            user_lat = bundle.getDouble(TAG1);
            user_lng = bundle.getDouble(TAG2);
        }


        binding.recViewCategory.setLayoutManager(new LinearLayoutManager(activity));
        MainItemData itemData1 = new MainItemData(0);
        mainItemDataList.add(itemData1);
        mainAdapter = new MainAdapter(mainItemDataList,activity,this,user_lat,user_lng);
        binding.recViewCategory.setAdapter(mainAdapter);

        MainItemData itemData2 = new MainItemData(1);
        mainItemDataList.add(itemData2);
        mainAdapter.notifyItemInserted(mainItemDataList.size()-1);
        binding.consSearch.setOnClickListener(v -> {

            Intent intent = new Intent(activity, ShopsActivity.class);
            intent.putExtra("lat", user_lat);
            intent.putExtra("lng", user_lng);
            intent.putExtra("type", true);
            startActivity(intent);
        });

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void stopTimer() {
        if (mainAdapter!=null){
            mainAdapter.stopTimer();
        }
    }


    public void placeItemData(NearbyModel.Result placeModel) {
        int type = 2;


        if (isRestaurant(placeModel)){
            if (type==1){
                //no menu
                Intent intent = new Intent(activity, ShopDetailsActivity.class);
                intent.putExtra("data",placeModel);
                startActivity(intent);

            }else if (type==2){
                // menu image only
                Intent intent = new Intent(activity, ShopDetailsActivity.class);
                intent.putExtra("data",placeModel);
                startActivity(intent);
            }else if (type==3){
                // menu products
            }
        }else {
            Intent intent = new Intent(activity, ShopMapActivity.class);
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
