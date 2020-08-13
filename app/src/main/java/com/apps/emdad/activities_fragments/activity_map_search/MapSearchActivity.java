package com.apps.emdad.activities_fragments.activity_map_search;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.adapters.FavoriteLocationAdapter;
import com.apps.emdad.adapters.KeywordsAdapter;
import com.apps.emdad.databinding.ActivityFilterBinding;
import com.apps.emdad.databinding.ActivityMapSearchBinding;
import com.apps.emdad.databinding.DialogAlertBinding;
import com.apps.emdad.databinding.DialogFavoriteLocationBinding;
import com.apps.emdad.interfaces.Listeners;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.DefaultSettings;
import com.apps.emdad.models.FavoriteLocationModel;
import com.apps.emdad.models.FilterModel;
import com.apps.emdad.models.KeywordModel;
import com.apps.emdad.models.PlaceGeocodeData;
import com.apps.emdad.models.PlaceMapDetailsData;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapSearchActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, Listeners.BackListener {
    private ActivityMapSearchBinding binding;
    private String lang;
    private boolean isSearchOpen = false;
    private boolean isMapOpen = false;
    private double lat = 0.0, lng = 0.0;
    private String address = "";
    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 15.0f;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private boolean canSelectLocation = false;
    private Preferences preferences;
    private List<FavoriteLocationModel> favoriteLocationModelList;
    private DefaultSettings defaultSettings;
    private FavoriteLocationAdapter adapter;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_map_search);
        initView();
    }

    private void initView() {
        favoriteLocationModelList = new ArrayList<>();
        preferences = Preferences.getInstance();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setListener(this);
        defaultSettings = preferences.getAppSetting(this);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoriteLocationAdapter(favoriteLocationModelList,this);
        binding.recView.setAdapter(adapter);


        if (defaultSettings!=null){
            favoriteLocationModelList.addAll(defaultSettings.getFavoriteLocationModelList());

        }
        if (favoriteLocationModelList.size()>0){
            binding.cardFavorite.setVisibility(View.VISIBLE);
        }


        binding.cardIconSearch.setOnClickListener(v -> {
            if (isSearchOpen) {

            }else {
                setUpAnimationSearch(0,(int) getResources().getDimension(R.dimen.map_search));

            }
            isSearchOpen =true;

        });
        binding.imageSearchClose.setOnClickListener(v -> {
            setUpAnimationSearch((int) getResources().getDimension(R.dimen.map_search),0);
            isSearchOpen = false;
        });
        binding.cardIconMap.setOnClickListener(v -> {
            if (isMapOpen) {

            }else {
                setUpAnimationMap(0,(int) getResources().getDimension(R.dimen.map_search));

            }
            isMapOpen =true;

        });
        binding.imageMapClose.setOnClickListener(v -> {
            setUpAnimationMap((int) getResources().getDimension(R.dimen.map_search),0);
            isMapOpen = false;
        });
        binding.btnMap.setOnClickListener(v -> {
            binding.btnMap.setBackgroundResource(R.color.colorPrimary);
            binding.btnMap.setTextColor(ContextCompat.getColor(this,R.color.white));

            binding.btnHybrid.setBackgroundResource(R.color.white);
            binding.btnHybrid.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));


            if (mMap!=null&&mMap.getMapType()==GoogleMap.MAP_TYPE_HYBRID){
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                onMapReady(mMap);
            }



        });
        binding.btnHybrid.setOnClickListener(v -> {

            binding.btnHybrid.setBackgroundResource(R.color.colorPrimary);
            binding.btnHybrid.setTextColor(ContextCompat.getColor(this,R.color.white));


            binding.btnMap.setBackgroundResource(R.color.white);
            binding.btnMap.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));



            if (mMap!=null&&mMap.getMapType()==GoogleMap.MAP_TYPE_NORMAL){
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                onMapReady(mMap);
            }



        });
        binding.imageCheckBox.setOnClickListener(v -> {
            createFavoriteLocationDialog();
        });
        binding.imageCloseSheet.setOnClickListener(v -> {
            closeSheet();
        });
        binding.cardFavorite.setOnClickListener(v -> {
            openSheet();
        });
        binding.btnConfirm.setOnClickListener(v -> {
            Intent intent = getIntent();
            FavoriteLocationModel model = new FavoriteLocationModel("","",address,lat,lng);
            intent.putExtra("data",model);
            setResult(RESULT_OK,intent);
            finish();
        });
        updateUI();




    }

    private void createFavoriteLocationDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogFavoriteLocationBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_favorite_location, null, false);
        binding.tvAddress.setText(address);
        binding.btnSave.setOnClickListener(v -> {
            MapSearchActivity.this.binding.imageCheckBox.setBackgroundResource(R.drawable.ic_star2);
            MapSearchActivity.this.binding.cardFavorite.setVisibility(View.VISIBLE);
            String name = binding.edtLocationName.getText().toString().trim();
            if (!name.isEmpty()){
                binding.edtLocationName.setError(null);
                Common.CloseKeyBoard(this,binding.edtLocationName);

                FavoriteLocationModel model = new FavoriteLocationModel(name,"fav",address,lat,lng);
                favoriteLocationModelList.add(model);
                adapter.notifyDataSetChanged();

                if (defaultSettings==null){
                    defaultSettings = new DefaultSettings();
                }
                defaultSettings.setFavoriteLocationModelList(favoriteLocationModelList);
                preferences.createUpdateAppSetting(this,defaultSettings);

                MapSearchActivity.this.binding.cardFavorite.setVisibility(View.VISIBLE);
                dialog.dismiss();


            }else {
                binding.edtLocationName.setError(getString(R.string.field_required));
            }

        });
        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        binding.flHome.setOnClickListener(v -> {
            FavoriteLocationModel model = new FavoriteLocationModel("","home",address,lat,lng);
            favoriteLocationModelList.add(model);
            adapter.notifyDataSetChanged();

            if (defaultSettings==null){
                defaultSettings = new DefaultSettings();
            }
            defaultSettings.setFavoriteLocationModelList(favoriteLocationModelList);
            preferences.createUpdateAppSetting(this,defaultSettings);
            MapSearchActivity.this.binding.cardFavorite.setVisibility(View.VISIBLE);
            MapSearchActivity.this.binding.imageCheckBox.setBackgroundResource(R.drawable.ic_star2);
            dialog.dismiss();
        });
        binding.flWork.setOnClickListener(v -> {

            FavoriteLocationModel model = new FavoriteLocationModel("","work",address,lat,lng);
            favoriteLocationModelList.add(model);
            adapter.notifyDataSetChanged();
            if (defaultSettings==null){
                defaultSettings = new DefaultSettings();
            }
            defaultSettings.setFavoriteLocationModelList(favoriteLocationModelList);
            preferences.createUpdateAppSetting(this,defaultSettings);
            MapSearchActivity.this.binding.cardFavorite.setVisibility(View.VISIBLE);
            MapSearchActivity.this.binding.imageCheckBox.setBackgroundResource(R.drawable.ic_star2);

            dialog.dismiss();
        });


        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());

        dialog.show();
    }



    private void setUpAnimationSearch(int start , int end) {

        ValueAnimator valueAnimator = ValueAnimator.ofInt(start,end);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = binding.cardSearch.getLayoutParams();
            params.width = (int) animation.getAnimatedValue();
            binding.cardSearch.setLayoutParams(params);
        });
        valueAnimator.start();
    }

    private void setUpAnimationMap(int start , int end) {

        ValueAnimator valueAnimator = ValueAnimator.ofInt(start,end);
        valueAnimator.setDuration(1000);
        valueAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = binding.cardMap.getLayoutParams();
            params.width = (int) animation.getAnimatedValue();
            binding.cardMap.setLayoutParams(params);
        });
        valueAnimator.start();

    }


    private void checkPermission()
    {
        if (ActivityCompat.checkSelfPermission(this,fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
        } else {
            mMap.setMyLocationEnabled(true);
            initGoogleApi();
        }
    }
    private void initGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    private void updateUI() {

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setMapType(mapType);
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);


            checkPermission();

            mMap.setOnMapClickListener(latLng -> {
                lat = latLng.latitude;
                lng = latLng.longitude;
                AddMarker(lat,lng);
                getGeoData(lat,lng);

            });
            mMap.setOnCameraMoveListener(() -> {
                mMap.clear();
                binding.pin.setVisibility(View.VISIBLE);
            });

            mMap.setOnCameraIdleListener(() ->{
                mMap.clear();
                getGeoData(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
                binding.pin.setVisibility(View.VISIBLE);
            });
        }
    }


    private void searchByPlaceId(String place_id){
        

    }
    private void Search(String query) {

        binding.progBar.setVisibility(View.VISIBLE);
        binding.imagePin.setVisibility(View.GONE);

        String fields = "id,place_id,name,geometry,formatted_address";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .searchOnMap("textquery", query, fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceMapDetailsData>() {
                    @Override
                    public void onResponse(Call<PlaceMapDetailsData> call, Response<PlaceMapDetailsData> response) {
                        binding.progBar.setVisibility(View.GONE);
                        binding.imagePin.setVisibility(View.VISIBLE);

                        if (response.isSuccessful() && response.body() != null) {


                            if (response.body().getCandidates().size() > 0) {

                                address = response.body().getCandidates().get(0).getFormatted_address().replace("Unnamed Road,", "");
                                binding.tvAddress.setText(address + "");
                                AddMarker(response.body().getCandidates().get(0).getGeometry().getLocation().getLat(), response.body().getCandidates().get(0).getGeometry().getLocation().getLng());
                            }
                        } else {
                            binding.progBar.setVisibility(View.GONE);
                            binding.imagePin.setVisibility(View.VISIBLE);

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceMapDetailsData> call, Throwable t) {
                        try {
                            binding.progBar.setVisibility(View.GONE);
                            binding.imagePin.setVisibility(View.VISIBLE);

                            Toast.makeText(MapSearchActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void getGeoData(final double lat, double lng) {
        binding.progBar.setVisibility(View.VISIBLE);
        binding.imagePin.setVisibility(View.GONE);
        String location = lat + "," + lng;
        Log.e("add",location);
        Api.getService("https://maps.googleapis.com/maps/api/")
                .getGeoData(location, lang, getString(R.string.search_key))
                .enqueue(new Callback<PlaceGeocodeData>() {
                    @Override
                    public void onResponse(Call<PlaceGeocodeData> call, Response<PlaceGeocodeData> response) {
                        binding.progBar.setVisibility(View.GONE);
                        binding.imagePin.setVisibility(View.VISIBLE);

                        if (response.isSuccessful() && response.body() != null) {

                            if (response.body().getResults().size() > 0) {
                                if (response.body().getResults().size()>1){
                                    address = response.body().getResults().get(1).getFormatted_address();

                                }else {
                                    address = response.body().getResults().get(0).getFormatted_address();

                                }
                                MapSearchActivity.this.binding.imageCheckBox.setBackgroundResource(R.drawable.ic_star_empty);
                                binding.tvAddress.setText(address + "");
                                binding.imageCheckBox.setVisibility(View.VISIBLE);
                                canSelectLocation = true;
                                binding.btnConfirm.setBackgroundResource(R.drawable.small_rounded_primary);
                            }
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceGeocodeData> call, Throwable t) {
                        try {
                            binding.imagePin.setVisibility(View.VISIBLE);
                            binding.progBar.setVisibility(View.GONE);

                            Toast.makeText(MapSearchActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void AddMarker(double lat, double lng) {

        this.lat = lat;
        this.lng = lng;



        if (marker == null) {
            binding.pin.setVisibility(View.GONE);
            View view = LayoutInflater.from(this).inflate(R.layout.map_pin,null);
            IconGenerator iconGenerator = new IconGenerator(this);
            iconGenerator.setBackground(null);
            iconGenerator.setContentView(view);
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())));
        }
        marker.setDraggable(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    private void initLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(60000);
        LocationSettingsRequest.Builder request = new LocationSettingsRequest.Builder();
        request.addLocationRequest(locationRequest);
        request.setAlwaysShow(false);


        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, request.build());
        result.setResultCallback(locationSettingsResult -> {
            Status status = locationSettingsResult.getStatus();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    startLocationUpdate();
                    break;

                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(MapSearchActivity.this,100);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (googleApiClient!=null)
        {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        AddMarker(lat,lng);
        getGeoData(lat,lng);

        if (googleApiClient!=null)
        {
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
            googleApiClient.disconnect();
            googleApiClient = null;
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == loc_req)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                mMap.setMyLocationEnabled(true);
                initGoogleApi();
            }else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100&&resultCode== Activity.RESULT_OK)
        {

            startLocationUpdate();
        }

    }


    private void openSheet() {
        binding.root.clearAnimation();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        binding.root.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.root.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void closeSheet() {
        binding.root.clearAnimation();

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        binding.root.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.root.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public void setFavoriteItem(FavoriteLocationModel model) {
        closeSheet();
        lat = model.getLat();
        lng = model.getLng();
        address = model.getAddress();
        binding.tvAddress.setText(address);
        AddMarker(lat,lng);

    }

    @Override
    public void back() {
        if (binding.root.getVisibility() == View.VISIBLE) {
            closeSheet();
        }else {
            super.onBackPressed();

        }
    }


    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient!=null)
        {
            if (locationCallback!=null)
            {
                LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
                googleApiClient.disconnect();
                googleApiClient = null;
            }
        }
    }


}


