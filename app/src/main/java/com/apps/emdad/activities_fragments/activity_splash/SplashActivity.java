package com.apps.emdad.activities_fragments.activity_splash;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.emdad.R;
import com.apps.emdad.databinding.ActivitySplashBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.share.App;
import com.google.android.material.tabs.TabLayout;

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
    private String  lang = "ar";


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

        constraintLayout = findViewById(R.id.layout);
        llLang = findViewById(R.id.llLang);
        cardAr = findViewById(R.id.cardAr);
        cardEn = findViewById(R.id.cardEn);
        tvAr = findViewById(R.id.tvAr);
        tvEn = findViewById(R.id.tvEn);

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


        cardAr.setOnClickListener(v -> {
            cardAr.setCardElevation(5f);
            cardEn.setCardElevation(0f);
            tvAr.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
            tvEn.setTextColor(ContextCompat.getColor(this,R.color.color2));

        });

        cardEn.setOnClickListener(v -> {
            cardAr.setCardElevation(0f);
            cardEn.setCardElevation(5f);
            tvAr.setTextColor(ContextCompat.getColor(this,R.color.color2));
            tvEn.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));

        });

    }
}