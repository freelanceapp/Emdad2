package com.apps.emdad.activities_fragments.activity_home.fragments;

import android.content.Intent;
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
import com.apps.emdad.activities_fragments.activity_add_coupon.AddCouponActivity;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_setting.SettingsActivity;
import com.apps.emdad.databinding.FragmentNotificationBinding;
import com.apps.emdad.databinding.FragmentProfileBinding;
import com.apps.emdad.interfaces.Listeners;

import java.util.Currency;
import java.util.Locale;

import io.paperdb.Paper;

public class Fragment_Profile extends Fragment implements Listeners.ProfileAction {
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
        binding.setActions(this);
        Currency currency = Currency.getInstance("sar");

    }

    @Override
    public void onReviews() {

    }

    @Override
    public void onFeedback() {

    }

    @Override
    public void onCoupons() {

    }

    @Override
    public void onAddCoupon() {
        Intent intent = new Intent(activity, AddCouponActivity.class);
        startActivityForResult(intent,100);

    }

    @Override
    public void onSetting() {
        Intent intent = new Intent(activity, SettingsActivity.class);
        startActivityForResult(intent,200);
    }

    @Override
    public void onPayment() {

    }

    @Override
    public void onTelegram() {

    }

    @Override
    public void onNotification() {

    }

    @Override
    public void logout() {

    }
}
