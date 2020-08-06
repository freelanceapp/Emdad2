package com.apps.emdad.activities_fragments.activity_home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_add_order.AddOrderActivity;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Main;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Notifications;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Profile;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Order;
import com.apps.emdad.activities_fragments.activity_login.LoginActivity;
import com.apps.emdad.activities_fragments.activity_verification_code.VerificationCodeActivity;
import com.apps.emdad.adapters.CountriesAdapter;
import com.apps.emdad.databinding.ActivityHomeBinding;
import com.apps.emdad.databinding.ActivityLoginBinding;
import com.apps.emdad.databinding.DialogCountriesBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CountryModel;
import com.apps.emdad.models.LoginModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.share.Common;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private FragmentManager fragmentManager;
    private Fragment_Main fragment_main;
    private Fragment_Order fragment_order;
    private Fragment_Notifications fragment_notifications;
    private Fragment_Profile fragment_profile;
    private UserModel userModel;
    private Preferences preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("Lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentManager = getSupportFragmentManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new TransitionSet());
            getWindow().setExitTransition(new TransitionSet());

        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        initView();
        if (savedInstanceState==null){
            displayFragmentMain();
        }
    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        binding.fab.setColorFilter(ContextCompat.getColor(this,R.color.white), PorterDuff.Mode.SRC_IN);


        binding.llStore.setOnClickListener(v -> {
            displayFragmentMain();
        });

        binding.llNotification.setOnClickListener(v -> {
            displayFragmentNotification();
        });

        binding.llOrder.setOnClickListener(v -> {
            displayFragmentOrder();
        });

        binding.llProfile.setOnClickListener(v -> {
            displayFragmentProfile();
        });

        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddOrderActivity.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,binding.fab,binding.fab.getTransitionName());
                startActivity(intent,options.toBundle());

            }else {
                startActivity(intent);

            }

        });



    }


    private void displayFragmentMain(){
        updateMainUi();

        if (fragment_main ==null){
            fragment_main = Fragment_Main.newInstance();
        }

        if (fragment_order!=null&&fragment_order.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }

        if (fragment_notifications!=null&&fragment_notifications.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_profile!=null&&fragment_profile.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_main.isAdded()){
            fragmentManager.beginTransaction().show(fragment_main).commit();
        }else {
            fragmentManager.beginTransaction().add(R.id.fragment_app,fragment_main,"fragment_main").addToBackStack("fragment_main").commit();
        }


    }
    private void displayFragmentNotification(){
        updateNotificationUi();


        if (fragment_notifications ==null){
            fragment_notifications = Fragment_Notifications.newInstance();
        }

        if (fragment_order!=null&&fragment_order.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }

        if (fragment_main!=null&&fragment_main.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }

        if (fragment_profile!=null&&fragment_profile.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_notifications.isAdded()){
            fragmentManager.beginTransaction().show(fragment_notifications).commit();
        }else {
            fragmentManager.beginTransaction().add(R.id.fragment_app,fragment_notifications,"fragment_notifications").addToBackStack("fragment_notifications").commit();
        }

    }
    private void displayFragmentOrder(){
        updateOrderUi();

        if (fragment_order ==null){
            fragment_order = Fragment_Order.newInstance();
        }

        if (fragment_main!=null&&fragment_main.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }

        if (fragment_notifications!=null&&fragment_notifications.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_profile!=null&&fragment_profile.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_profile).commit();
        }


        if (fragment_order.isAdded()){
            fragmentManager.beginTransaction().show(fragment_order).commit();
        }else {
            fragmentManager.beginTransaction().add(R.id.fragment_app,fragment_order,"fragment_order").addToBackStack("fragment_order").commit();
        }

    }
    private void displayFragmentProfile(){
        updateProfileUi();

        if (fragment_profile ==null){
            fragment_profile = Fragment_Profile.newInstance();
        }

        if (fragment_order!=null&&fragment_order.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_order).commit();
        }

        if (fragment_notifications!=null&&fragment_notifications.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_notifications).commit();
        }

        if (fragment_main!=null&&fragment_main.isAdded()){
            fragmentManager.beginTransaction().hide(fragment_main).commit();
        }


        if (fragment_profile.isAdded()){
            fragmentManager.beginTransaction().show(fragment_profile).commit();
        }else {
            fragmentManager.beginTransaction().add(R.id.fragment_app,fragment_profile,"fragment_profile").addToBackStack("fragment_profile").commit();
        }


    }


    private void updateMainUi(){
        binding.iconStore.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary));
        binding.iconNotification.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
        binding.iconOrder.setColorFilter(ContextCompat.getColor(this,R.color.gray4));

        binding.tvStore.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this,R.color.gray4));

    }

    private void updateNotificationUi(){

        binding.iconStore.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
        binding.iconNotification.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary));
        binding.iconOrder.setColorFilter(ContextCompat.getColor(this,R.color.gray4));

        binding.tvStore.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this,R.color.gray4));

    }

    private void updateOrderUi(){


        binding.iconStore.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
        binding.iconNotification.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
        binding.iconOrder.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary));

        binding.tvStore.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this,R.color.gray4));
    }

    private void updateProfileUi(){

        binding.iconStore.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
        binding.iconNotification.setColorFilter(ContextCompat.getColor(this,R.color.gray4));
        binding.iconOrder.setColorFilter(ContextCompat.getColor(this,R.color.gray4));

        binding.tvStore.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvNotification.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvOrder.setTextColor(ContextCompat.getColor(this,R.color.gray4));
        binding.tvProfile.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment :fragments){
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment :fragments){
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onBackPressed() {
        if (fragment_main!=null&&fragment_main.isAdded()&&fragment_main.isVisible()){
            if (userModel==null){
                navigateToLoginActivity();
            }else {
                finish();
            }
        }else {
            displayFragmentMain();
        }
    }

    private void navigateToLoginActivity() {

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
