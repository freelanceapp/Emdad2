package com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order;

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
import com.apps.emdad.adapters.MyPagerAdapter;
import com.apps.emdad.databinding.FragmentOrdersBinding;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Order extends Fragment {
    private FragmentOrdersBinding binding;
    private MyPagerAdapter adapter;
    private List<Fragment> fragmentList;
    private List<String> titles;
    private HomeActivity activity;


    public static Fragment_Order newInstance(){
        return new Fragment_Order();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_orders,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        fragmentList = new ArrayList<>();
        titles = new ArrayList<>();



        binding.tab.setupWithViewPager(binding.pager);
        fragmentList.add(Fragment_Current_Order.newInstance());
        fragmentList.add(Fragment_Delivering_Order.newInstance());
        titles.add(getString(R.string.order));
        titles.add(getString(R.string.delivering));
        adapter = new MyPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,fragmentList,titles);
        binding.pager.setAdapter(adapter);
    }
}
