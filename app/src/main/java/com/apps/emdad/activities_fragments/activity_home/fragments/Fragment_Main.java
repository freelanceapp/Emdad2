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
import androidx.fragment.app.FragmentPagerAdapter;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Current_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Delivering_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Order;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.adapters.MyPagerAdapter;
import com.apps.emdad.databinding.FragmentMainBinding;
import com.apps.emdad.databinding.FragmentOrdersBinding;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Main extends Fragment {
    private final static String TAG1 ="lat";
    private final static String TAG2 ="lng";
    private double user_lat=0.0,user_lng=0.0;
    private HomeActivity activity;
    private FragmentMainBinding binding;


    public static Fragment_Main newInstance(double user_lat,double user_lng){
        Bundle bundle = new Bundle();
        bundle.putDouble(TAG1,user_lat);
        bundle.putDouble(TAG2,user_lng);
        Fragment_Main fragment_main  =new Fragment_Main();
        fragment_main.setArguments(bundle);
        return fragment_main;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();

        Bundle bundle = getArguments();
        if (bundle!=null){
            user_lat = bundle.getDouble(TAG1);
            user_lng = bundle.getDouble(TAG2);
        }
        binding.consSearch.setOnClickListener(v -> {

            Intent intent = new Intent(activity, ShopsActivity.class);
            intent.putExtra("lat", user_lat);
            intent.putExtra("lng", user_lng);
            intent.putExtra("type",true);
            startActivity(intent);
        });
    }
}
