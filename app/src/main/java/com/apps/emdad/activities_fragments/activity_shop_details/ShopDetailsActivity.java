package com.apps.emdad.activities_fragments.activity_shop_details;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.adapters.HoursAdapter;
import com.apps.emdad.adapters.ImagePagerAdapter;
import com.apps.emdad.databinding.ActivityShopDetailsBinding;
import com.apps.emdad.databinding.DialogAlertBinding;
import com.apps.emdad.databinding.DialogHoursBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.HourModel;
import com.apps.emdad.models.NearbyModel;
import com.apps.emdad.models.PhotosModel;
import com.apps.emdad.models.PlaceDetailsModel;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopDetailsActivity extends AppCompatActivity {
    private ActivityShopDetailsBinding binding;
    private ImagePagerAdapter imagePagerAdapter;
    private List<PhotosModel> photosModelList;
    private NearbyModel.Result placeModel;
    private String lang;
    private List<HourModel> hourModelList;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_details);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        placeModel = (NearbyModel.Result) intent.getSerializableExtra("data");

    }

    private void initView() {
        hourModelList = new ArrayList<>();
        photosModelList = new ArrayList<>();
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.setDistance("");
        binding.flBack.setOnClickListener(v -> finish());
        binding.progBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this,R.color.colorPrimary), PorterDuff.Mode.SRC_IN);



        binding.tvShow.setOnClickListener(v -> {
            if (hourModelList.size()>0){
                createDialogAlert();
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
                            Toast.makeText(ShopDetailsActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
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
        if (body.getResult().getPhotos()!=null&&body.getResult().getPhotos().size()>0){
            placeModel.setPhotosModels(body.getResult().getPhotos());
            photosModelList.clear();
            photosModelList.addAll(placeModel.getPhotosModels());
            imagePagerAdapter = new ImagePagerAdapter(photosModelList,this);
            binding.pager.setAdapter(imagePagerAdapter);
            binding.tab.setupWithViewPager(binding.pager);

            for(int i=0; i < binding.tab.getTabCount(); i++) {
                View tab = ((ViewGroup) binding.tab.getChildAt(0)).getChildAt(i);
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
                p.setMargins(2, 0, 2, 0);
                tab.requestLayout();
            }

        }else {
            placeModel.setPhotosModels(new ArrayList<>());
        }

        if (body.getResult().getOpening_hours() != null) {
            placeModel.setOpen(body.getResult().getOpening_hours().isOpen_now());
            if (body.getResult().getOpening_hours().getWeekday_text()!=null&&body.getResult().getOpening_hours().getWeekday_text().size()>0){
                placeModel.setWork_hours(body.getResult().getOpening_hours());

                placeModel.setOpen(true);

                hourModelList.clear();
                hourModelList.addAll(getHours());
                if (hourModelList.size()>0){
                    binding.tvHours.setText(hourModelList.get(0).getTime());

                }

            }else {
                placeModel.setOpen(false);

            }


        } else {
            placeModel.setOpen(false);

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



        binding.setDistance(String.format(Locale.ENGLISH,"%.2f",placeModel.getDistance()));
        binding.setModel(placeModel);
        binding.ll.setVisibility(View.VISIBLE);
        binding.progBar.setVisibility(View.GONE);
        binding.ll.setVisibility(View.VISIBLE);
        binding.imageShare.setVisibility(View.VISIBLE);
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


    private void createDialogAlert() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        HoursAdapter adapter = new HoursAdapter(hourModelList,this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }
}