package com.apps.emdad.activities_fragments.activity_home.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.databinding.FragmentNotificationBinding;
import com.apps.emdad.databinding.FragmentProfileBinding;

import java.util.Currency;
import java.util.Locale;

import io.paperdb.Paper;

public class Fragment_Profile extends Fragment {
    private FragmentProfileBinding binding;
    private HomeActivity activity;
    private String lang;

    public static Fragment_Profile newInstance(){
        return new Fragment_Profile();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        Paper.init(activity);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        Currency currency = Currency.getInstance("sar");

    }
}
