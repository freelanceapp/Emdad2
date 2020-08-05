package com.apps.emdad.activities_fragments.activity_home.fragments;

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
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Current_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Delivering_Order;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Order;
import com.apps.emdad.adapters.MyPagerAdapter;
import com.apps.emdad.databinding.FragmentMainBinding;
import com.apps.emdad.databinding.FragmentOrdersBinding;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Main extends Fragment {
    private FragmentMainBinding binding;


    public static Fragment_Main newInstance(){
        return new Fragment_Main();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {

    }
}
