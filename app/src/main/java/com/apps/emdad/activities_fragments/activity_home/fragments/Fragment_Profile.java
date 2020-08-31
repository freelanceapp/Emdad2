package com.apps.emdad.activities_fragments.activity_home.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Currency;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Profile extends Fragment implements Listeners.ProfileAction {
    private FragmentProfileBinding binding;
    private HomeActivity activity;
    private String lang;
    private UserModel userModel;
    private Preferences preferences;

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

        preferences = Preferences.getInstance();
        userModel =preferences.getUserData(activity);

        Paper.init(activity);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setActions(this);
        binding.setModel(userModel);

        if (userModel!=null){
            if (userModel.getUser().getLogo() != null) {

                Picasso.get().load(Uri.parse(Tags.IMAGE_URL + userModel.getUser().getLogo())).placeholder(R.drawable.image_avatar).into(binding.image);
            } else {
                Picasso.get().load(R.drawable.image_avatar).into(binding.image);

            }
        }

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

        activity.logout();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == Activity.RESULT_OK ) {
            activity.refreshActivity();
        }
    }
}
