package com.apps.emdad.activities_fragments.activity_splash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_intro_slider.IntroSliderActivity;
import com.apps.emdad.activities_fragments.activity_login.LoginActivity;
import com.apps.emdad.databinding.ActivitySplashBinding;
import com.apps.emdad.interfaces.Listeners;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.DefaultSettings;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.share.App;
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
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding binding;
    private ConstraintSet constraintSetOld = new ConstraintSet();
    private ConstraintSet constraintSetNew = new ConstraintSet();
    private ConstraintLayout constraintLayout;
    private LinearLayout llLang;
    private boolean status;
    private CardView cardAr,cardEn;
    private TextView tvAr,tvEn;
    private Button btnNext;
    private String lang = "ar";
    private Preferences preferences;



    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_splash);
        initView();
    }

    private void initView() {
        preferences = Preferences.getInstance();
        constraintLayout = findViewById(R.id.layout);
        btnNext = findViewById(R.id.btnNext);
        llLang = findViewById(R.id.llLang);
        cardAr = findViewById(R.id.cardAr);
        cardEn = findViewById(R.id.cardEn);
        tvAr = findViewById(R.id.tvAr);
        tvEn = findViewById(R.id.tvEn);


        if (preferences.getAppSetting(this)!=null&&preferences.getAppSetting(this).isLanguageSelected()){

            if (preferences.getAppSetting(this).isShowIntroSlider()){
                Intent intent = new Intent(this, IntroSliderActivity.class);
                startActivity(intent);
                finish();
            }else {

                if (preferences.getSession(this).equals(Tags.session_login)){
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }else {

                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    },1500);

                }


            }


        }else {
            constraintSetOld.clone(constraintLayout);
            constraintSetNew.clone(this,R.layout.language_layout);
            new Handler().postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Transition transition = new ChangeBounds();
                    transition.setDuration(400);
                    transition.setInterpolator(new AccelerateDecelerateInterpolator());
                    TransitionManager.beginDelayedTransition(constraintLayout,transition);

                }

                if (!status){
                    constraintSetNew.applyTo(constraintLayout);
                }else {
                    constraintSetOld.applyTo(constraintLayout);

                }
                status =!status;


                Animation animation = AnimationUtils.loadAnimation(SplashActivity.this,R.anim.lanuch);
                llLang.startAnimation(animation);

            },1000);
        }


        cardAr.setOnClickListener(v -> {
            cardAr.setCardElevation(5f);
            cardEn.setCardElevation(0f);
            tvAr.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
            tvEn.setTextColor(ContextCompat.getColor(this,R.color.color2));
            lang = "ar";

        });
        cardEn.setOnClickListener(v -> {
            cardAr.setCardElevation(0f);
            cardEn.setCardElevation(5f);
            tvAr.setTextColor(ContextCompat.getColor(this,R.color.color2));
            tvEn.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
            lang = "en";
        });
        btnNext.setOnClickListener(v -> {
            refreshActivity();
        });

    }

    private void refreshActivity() {
        Paper.init(this);
        Paper.book().write("lang",lang);
        DefaultSettings defaultSettings = new DefaultSettings();
        defaultSettings.setLanguageSelected(true);
        preferences.createUpdateAppSetting(this,defaultSettings);
        Intent intent = getIntent();
        finish();
        startActivity(intent);

    }








}