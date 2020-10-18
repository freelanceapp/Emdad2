package com.apps.emdad.activities_fragments.activity_driver_update_location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_delegate_add_offer.DelegateAddOfferActivity;
import com.apps.emdad.activities_fragments.activity_map_delivery_location.MapDeliveryLocationActivity;
import com.apps.emdad.databinding.ActivityDelegateAddOfferBinding;
import com.apps.emdad.databinding.ActivityDriverUpdateLocationBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.FromToLocationModel;
import com.apps.emdad.models.OrderModel;
import com.apps.emdad.models.RangeOfferModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.Locale;

import io.paperdb.Paper;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverUpdateLocationActivity extends AppCompatActivity implements OnMapReadyCallback  {
    private ActivityDriverUpdateLocationBinding binding;
    private String lang;
    private GoogleMap mMap;
    private String currency;
    private UserModel userModel;
    private Preferences preferences;
    private OrderModel orderModel;
    private final String fineLocPerm = Manifest.permission.ACCESS_FINE_LOCATION;
    private final int loc_req = 1225;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_update_location);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        orderModel = (OrderModel) intent.getSerializableExtra("data");


    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        currency = getString(R.string.sar);
        if (userModel!=null){
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setModel(orderModel);
        binding.close.setOnClickListener(v -> {super.onBackPressed();});
        updateUI();

    }

    private void checkPermission()
    {
        if (ActivityCompat.checkSelfPermission(this,fineLocPerm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{fineLocPerm}, loc_req);
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void updateUI() {

        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (googleMap != null) {
            mMap = googleMap;
            mMap.setTrafficEnabled(false);
            mMap.setBuildingsEnabled(false);
            mMap.setIndoorEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            addMarker(Double.parseDouble(orderModel.getMarket_latitude()),Double.parseDouble(orderModel.getMarket_longitude()),1);
            addMarker(Double.parseDouble(orderModel.getClient_latitude()),Double.parseDouble(orderModel.getClient_longitude()),2);
            checkPermission();
            mMap.setOnMyLocationChangeListener(location -> updateLocation(location));


        }
    }

    private void updateLocation(Location location) {
        Api.getService(Tags.base_url).updateDriverLocation(userModel.getUser().getToken(), orderModel.getDriver().getId(), orderModel.getId(),location.getLatitude(),location.getLongitude())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                            }
                        } else {
                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(DriverUpdateLocationActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(DriverUpdateLocationActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void addMarker(double lat ,double lng,int type) {
        View view = LayoutInflater.from(this).inflate(R.layout.map_add_offer_location_row,null);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        if (type==1){
            tvTitle.setText(getString(R.string.pick_up));
            tvTitle.setBackgroundResource(R.drawable.rounded_primary);
        }else {
            tvTitle.setText(getString(R.string.drop_off));
            tvTitle.setBackgroundResource(R.drawable.rounded_second);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(Double.parseDouble(orderModel.getMarket_latitude()),Double.parseDouble(orderModel.getMarket_longitude())));
            builder.include(new LatLng(Double.parseDouble(orderModel.getClient_latitude()),Double.parseDouble(orderModel.getClient_longitude())));

            try {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),150));

            }catch (Exception e){

            }

        }
        IconGenerator iconGenerator = new IconGenerator(this);
        iconGenerator.setContentPadding(2,2,2,2);
        iconGenerator.setBackground(null);
        iconGenerator.setContentView(view);

        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())).position(new LatLng(lat,lng)));



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
            }else
            {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}




