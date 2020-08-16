package com.apps.emdad.activities_fragments.activity_home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.Explode;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_add_order.AddOrderActivity;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Main;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Notifications;
import com.apps.emdad.activities_fragments.activity_home.fragments.Fragment_Profile;
import com.apps.emdad.activities_fragments.activity_home.fragments.fragment_order.Fragment_Order;
import com.apps.emdad.activities_fragments.activity_login.LoginActivity;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.activities_fragments.activity_splash.SplashActivity;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks, LocationListener {
    private ActivityHomeBinding binding;
    private FragmentManager fragmentManager;
    private Fragment_Main fragment_main;
    private Fragment_Order fragment_order;
    private Fragment_Notifications fragment_notifications;
    private Fragment_Profile fragment_profile;
    private UserModel userModel;
    private Preferences preferences;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String gps_perm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 22;
    private Location location;



    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
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
            CheckPermission();
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

            intent.putExtra("lat",location.getLatitude());
            intent.putExtra("lng",location.getLongitude());
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




    private void initGoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    private void CheckPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, gps_perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{gps_perm}, loc_req);
        } else {

            initGoogleApiClient();

        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment :fragments){
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == loc_req)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                initGoogleApiClient();
            }else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initLocationRequest()
    {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(60000);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());

        result.setResultCallback(result1 -> {

            Status status = result1.getStatus();
            switch (status.getStatusCode())
            {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdate();
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(HomeActivity.this,1255);
                    }catch (Exception e)
                    {
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Log.e("not available","not available");
                    break;
            }
        });

    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdate()
    {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient!=null){
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        binding.flLoading.setVisibility(View.GONE);
        displayFragmentMain();

        if (googleApiClient!=null){
            googleApiClient.disconnect();
        }
        if (locationCallback!=null){
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment :fragments){
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode==1255&&resultCode==RESULT_OK){
            startLocationUpdate();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback!=null)
        {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);

        }
        if (googleApiClient!=null)
        {
            googleApiClient.disconnect();
        }

    }
}
