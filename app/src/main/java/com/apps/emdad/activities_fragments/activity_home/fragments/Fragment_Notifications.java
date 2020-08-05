package com.apps.emdad.activities_fragments.activity_home.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.apps.emdad.R;
import com.apps.emdad.databinding.FragmentMainBinding;
import com.apps.emdad.databinding.FragmentNotificationBinding;

public class Fragment_Notifications extends Fragment {
    private FragmentNotificationBinding binding;


    public static Fragment_Notifications newInstance(){
        return new Fragment_Notifications();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {

    }
}
