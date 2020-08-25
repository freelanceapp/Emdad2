package com.apps.emdad.activities_fragments.activity_shop_map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.adapters.HoursAdapter;
import com.apps.emdad.adapters.ImagePagerAdapter;
import com.apps.emdad.databinding.ActivityShopDetailsBinding;
import com.apps.emdad.databinding.ActivityShopMapBinding;
import com.apps.emdad.databinding.DialogHoursBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.HourModel;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.models.PhotosModel;
import com.apps.emdad.models.PlaceDetailsModel;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityShopMapBinding binding;
    private NearbyModel.Result placeModel;
    private String lang;
    private List<HourModel> hourModelList;
    private HoursAdapter adapter;
    private GoogleMap mMap;
    private Bitmap bitmap = null;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_map);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        placeModel = (NearbyModel.Result) intent.getSerializableExtra("data");

    }

    private void initView() {
        hourModelList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setDistance("");
        binding.setModel(placeModel);
        binding.flBack.setOnClickListener(v -> finish());
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);



        binding.tvShow.setOnClickListener(v -> {
            if (hourModelList.size()>0){
                if (binding.expandLayout.isExpanded()){
                    binding.expandLayout.collapse(true);
                    binding.arrow2.animate().rotation(180).setDuration(500).start();
                }else {
                    binding.expandLayout.expand(true);
                    binding.arrow2.animate().rotation(0).setDuration(500).start();

                }
            }else {
                Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
            }
        });

        binding.imageShare.setOnClickListener(v -> {
            String url = Tags.base_url+"/"+placeModel.getPlace_id();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,url);
            startActivity(Intent.createChooser(intent,"Share"));
        });
        updateUI();
        getPlaceDetails();
    }

    private void getPlaceDetails() {

        String fields = "opening_hours,photos,reviews";

        Api.getService("https://maps.googleapis.com/maps/api/")
                .getPlaceDetails(placeModel.getPlace_id(), fields, lang, getString(R.string.map_api_key))
                .enqueue(new Callback<PlaceDetailsModel>() {
                    @Override
                    public void onResponse(Call<PlaceDetailsModel> call, Response<PlaceDetailsModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            updateHoursUI(response.body());
                        } else {

                            try {
                                Log.e("error_code", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<PlaceDetailsModel> call, Throwable t) {
                        try {

                            Log.e("Error", t.getMessage());
                            Toast.makeText(ShopMapActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {

                        }
                    }
                });
    }

    private void updateHoursUI(PlaceDetailsModel body) {

        if (body.getResult().getReviews()!=null&&body.getResult().getReviews().size()>0){
            placeModel.setReviews(body.getResult().getReviews());

        }else {
            placeModel.setReviews(new ArrayList<>());
        }


        if (body.getResult().getOpening_hours() != null) {
            placeModel.setOpen(body.getResult().getOpening_hours().isOpen_now());
            if (body.getResult().getOpening_hours().getWeekday_text()!=null&&body.getResult().getOpening_hours().getWeekday_text().size()>0){
                placeModel.setWork_hours(body.getResult().getOpening_hours());

                placeModel.setOpen(true);
                binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary));
                hourModelList.clear();
                hourModelList.addAll(getHours());


            }else {
                placeModel.setOpen(false);
                binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_red));
                binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_red));

            }


        } else {
            placeModel.setOpen(false);
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_red));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_red));


        }
        if (placeModel.getPhotos()!=null){
            if (placeModel.getPhotos().size()>0)
            {
                String url = Tags.IMAGE_Places_URL+placeModel.getPhotos().get(0).getPhoto_reference()+"&key="+getString(R.string.map_api_key);
                Picasso.get().load(Uri.parse(url)).fit().into(binding.image);

            }else
            {
                Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(binding.image);

            }
        }
        else {
            Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(binding.image);

        }


        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HoursAdapter(hourModelList,this);
        binding.recView.setAdapter(adapter);

        binding.setDistance(String.format(Locale.ENGLISH,"%.2f",placeModel.getDistance()));
        binding.setModel(placeModel);
        binding.progBar.setVisibility(View.GONE);
        binding.tvShow.setVisibility(View.VISIBLE);
        binding.arrow2.setVisibility(View.VISIBLE);
    }

    private List<HourModel> getHours() {
        List<HourModel> list = new ArrayList<>();

        for (String time: placeModel.getWork_hours().getWeekday_text()){

            String day = time.split(":", 2)[0].trim();
            String t = time.split(":",2)[1].trim();
            HourModel hourModel = new HourModel(day,t);
            list.add(hourModel);




        }

        return list;
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
            getBitmapImage(new LatLng(placeModel.getGeometry().getLocation().getLat(),placeModel.getGeometry().getLocation().getLng()));

        }
    }

    private void addMarker(Bitmap bitmap,LatLng latLng){

        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker()));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18.0f));






    }


    private void getBitmapImage(LatLng latLng){

        View view = LayoutInflater.from(this).inflate(R.layout.marker_shop,null);
        TextView tvAddress = view.findViewById(R.id.tvAddress);
        TextView tvDistance = view.findViewById(R.id.tvDistance);
        ImageView imageView = view.findViewById(R.id.image);

        tvAddress.setText(placeModel.getVicinity());
        tvDistance.setText(String.format(Locale.ENGLISH,"%.2f %s",placeModel.getDistance(),getString(R.string.km)));
        if (placeModel.getPhotos()!=null){
            Log.e("iii","iii");
            if (placeModel.getPhotos().size()>0)
            {
                addMarker(bitmap,latLng);

                Log.e("nnn","uuu");
                String url = Tags.IMAGE_Places_URL+placeModel.getPhotos().get(0).getPhoto_reference()+"&key="+getString(R.string.map_api_key);
                Picasso.get().load(Uri.parse(url)).fit().into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        //DisplayMetrics displayMetrics = new DisplayMetrics();
                        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        //view.setLayoutParams(new ViewGroup.LayoutParams(180, ViewGroup.LayoutParams.WRAP_CONTENT));
                        view.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
                        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
                        view.buildDrawingCache();
                        bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        Drawable drawable = view.getBackground();
                        if (drawable!=null){
                            drawable.draw(canvas);
                        }
                        view.draw(canvas);
                        addMarker(bitmap,latLng);
                        Log.e("1","1");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("error1",e.getMessage()+"__");
                    }
                });

            }else
            {
                Log.e("yyy","yyy");
                Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        //DisplayMetrics displayMetrics = new DisplayMetrics();
                        //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        //view.setLayoutParams(new ViewGroup.LayoutParams(180, ViewGroup.LayoutParams.WRAP_CONTENT));
                        view.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
                        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
                        view.buildDrawingCache();
                        bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        Drawable drawable = view.getBackground();
                        if (drawable!=null){
                            drawable.draw(canvas);
                        }
                        view.draw(canvas);
                        addMarker(bitmap,latLng);
                        Log.e("2","2");

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("error2",e.getMessage()+"__");

                    }
                });

            }
        }
        else {
            Log.e("ggg","gggg");
            Picasso.get().load(Uri.parse(placeModel.getIcon())).fit().into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    //DisplayMetrics displayMetrics = new DisplayMetrics();
                    //getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    //view.setLayoutParams(new ViewGroup.LayoutParams(180, ViewGroup.LayoutParams.WRAP_CONTENT));
                    view.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
                    view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
                    view.buildDrawingCache();
                    bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    Drawable drawable = view.getBackground();
                    if (drawable!=null){
                        drawable.draw(canvas);
                    }
                    view.draw(canvas);
                    addMarker(bitmap,latLng);
                    Log.e("3","3");

                }

                @Override
                public void onError(Exception e) {
                    Log.e("error3",e.getMessage()+"__");

                }
            });

        }


    }
}