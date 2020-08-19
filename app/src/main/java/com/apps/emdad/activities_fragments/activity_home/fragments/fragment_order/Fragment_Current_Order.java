package com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.adapters.MyPagerAdapter;
import com.apps.emdad.databinding.CurrentPreviousDeliveringLayoutBinding;
import com.apps.emdad.databinding.FragmentOrdersBinding;

import java.util.List;

public class Fragment_Current_Order extends Fragment {
    private HomeActivity activity;
    private CurrentPreviousDeliveringLayoutBinding binding;

    public static Fragment_Current_Order newInstance(){
        return new Fragment_Current_Order();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.current_previous_delivering_layout,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        binding.btnBack.setOnClickListener(v -> activity.displayFragmentMain());
    }
}
