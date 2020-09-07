package com.apps.emdad.activities_fragments.activity_shop_products;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_add_order_text.AddOrderTextActivity;
import com.apps.emdad.activities_fragments.activity_image.ImageActivity;
import com.apps.emdad.activities_fragments.activity_login.LoginActivity;
import com.apps.emdad.adapters.CustomHoursAdapter;
import com.apps.emdad.adapters.CustomImagePagerAdapter;
import com.apps.emdad.adapters.MenuImageAdapter;
import com.apps.emdad.databinding.ActivityCustomShopDetailsBinding;
import com.apps.emdad.databinding.ActivityShopProductsBinding;
import com.apps.emdad.databinding.DialogHoursBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CustomPlaceModel;
import com.apps.emdad.models.CustomShopDataModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.tags.Tags;
import com.google.android.material.appbar.AppBarLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.paperdb.Paper;

public class ShopProductActivity extends AppCompatActivity {
    private ActivityShopProductsBinding binding;
    private CustomShopDataModel placeModel;
    private String lang;
    private List<CustomPlaceModel.Days> hourModelList;
    private boolean canSend = false;
    private UserModel userModel;
    private Preferences preferences;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_shop_products);
        getDataFromIntent();
        initView();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        userModel = preferences.getUserData(this);

    }

    private void getDataFromIntent()
    {

        Intent intent = getIntent();
        placeModel = (CustomShopDataModel) intent.getSerializableExtra("data");

    }
    private void initView()
    {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        hourModelList = new ArrayList<>();
        String currency = getString(R.string.sar);
        if (userModel != null) {
            currency = userModel.getUser().getCountry().getWord().getCurrency();
        }
        binding.setCurrency(currency);

        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);

        binding.appBar.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
               int total = appBarLayout.getTotalScrollRange()+verticalOffset;
               if (total==0){
               }else {

               }
            }
        });

        binding.flBack.setOnClickListener(v -> finish());


        binding.tvShow.setOnClickListener(v -> {
            /*if (hourModelList.size()>0){
                createDialogAlert();
            }else {
                Toast.makeText(this, R.string.work_hour_not_aval, Toast.LENGTH_SHORT).show();
            }*/
        });

        binding.imageShare.setOnClickListener(v -> {
            String url = Tags.base_url+placeModel.getShop_id();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,url);
            startActivity(Intent.createChooser(intent,"Share"));
        });

        binding.flChooseFromMenu.setOnClickListener(v -> {
            if (canSend){
                Intent intent = new Intent(this, AddOrderTextActivity.class);
                intent.putExtra("data",placeModel);
                startActivityForResult(intent,100);

            }
        });

        binding.consReview.setOnClickListener(v -> {
            if (userModel==null){
                Intent intent = new Intent(this, LoginActivity.class);
                intent.putExtra("from",false);
                startActivity(intent);
            }else {

            }
        });

        binding.flBack.setOnClickListener(v -> {super.onBackPressed();});

        updateUI();
    }

    private void updateUI()
    {
        if (placeModel.getDays() != null&&placeModel.getDays().size()>0) {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.gray11));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.gray11));
            hourModelList.clear();
            hourModelList.addAll(placeModel.getDays());
            if (hourModelList.size()>0){
                binding.tvHours.setText(String.format("%s%s%s",hourModelList.get(0).getFrom_time(),"-",hourModelList.get(0).getTo_time()));

            }


        } else {
            binding.tvStatus.setTextColor(ContextCompat.getColor(this,R.color.color_rose));
            binding.icon.setColorFilter(ContextCompat.getColor(this,R.color.color_rose));



        }





        binding.setModel(placeModel);
        binding.imageShare.setVisibility(View.VISIBLE);
        binding.progBar.setVisibility(View.GONE);
        binding.llContainer.setVisibility(View.VISIBLE);


    }

    private void createDialogAlert()
    {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogHoursBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_hours, null, false);
        binding.recVeiw.setLayoutManager(new LinearLayoutManager(this));
        CustomHoursAdapter adapter = new CustomHoursAdapter(hourModelList,this);
        binding.recVeiw.setAdapter(adapter);

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private boolean isOpen(){
        if (placeModel.getDays().get(0).getStatus().equals("open")){
            return true;
        }

        return false;
    }

}