package com.apps.emdad.activities_fragments.activity_intro_slider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_login.LoginActivity;
import com.apps.emdad.adapters.IntroAdapter;
import com.apps.emdad.databinding.ActivityIntroSliderBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.DefaultSettings;
import com.apps.emdad.preferences.Preferences;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class IntroSliderActivity extends AppCompatActivity {
    private ActivityIntroSliderBinding binding;
    private IntroAdapter adapter;
    private Preferences preferences;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase,Paper.book().read("lang","ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Transition transition = new Fade();
            transition.setInterpolator(new LinearInterpolator());
            transition.setDuration(1000);
            getWindow().setEnterTransition(transition);
            getWindow().setExitTransition(transition);

        }

        binding = DataBindingUtil.setContentView(this,R.layout.activity_intro_slider);
        initView();
    }

    private void initView() {

        preferences = Preferences.getInstance();
        binding.tab.setupWithViewPager(binding.pager);
        adapter = new IntroAdapter(this);
        binding.pager.setAdapter(adapter);
        binding.pager.setOffscreenPageLimit(3);


        binding.tvTitle.setText(Html.fromHtml(getString(R.string.welcome_in_emdad)));
        binding.tvContent.setText(getString(R.string.we_deliver_order));



        binding.pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffset!=0){
                    AlphaAnimation alphaAnimation = new AlphaAnimation(0,positionOffset);
                    binding.llContent.startAnimation(alphaAnimation);
                }
            }

            @Override
            public void onPageSelected(int position) {


                switch (position){
                    case 0:
                        binding.btnStart.setVisibility(View.GONE);
                        binding.cons.setVisibility(View.VISIBLE);
                        binding.tvTitle.setText(Html.fromHtml(getString(R.string.welcome_in_emdad)));
                        binding.tvContent.setText(getString(R.string.we_deliver_order));
                        break;
                    case 1:
                        binding.btnStart.setVisibility(View.GONE);
                        binding.cons.setVisibility(View.VISIBLE);
                        binding.tvTitle.setText(Html.fromHtml(getString(R.string.get_discounts)));
                        binding.tvContent.setText(getString(R.string.many_discount));
                        break;

                    case 2:
                        binding.btnStart.setVisibility(View.VISIBLE);
                        binding.cons.setVisibility(View.GONE);
                        binding.tvTitle.setText(Html.fromHtml(getString(R.string.easy_comm)));
                        binding.tvContent.setText(getString(R.string.delegate_contact));
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (binding.pager.getCurrentItem()<adapter.getCount()){
                binding.pager.setCurrentItem(binding.pager.getCurrentItem()+1,true);
            }
        });

        binding.btnSkip.setOnClickListener(v -> {
           start();

        });


        binding.btnStart.setOnClickListener(v -> {
            start();
        });

    }

    private void start(){
        DefaultSettings defaultSettings = preferences.getAppSetting(this);
        if (defaultSettings!=null){
            defaultSettings.setShowIntroSlider(false);
            preferences.createUpdateAppSetting(this,defaultSettings);
        }


        Intent intent = new Intent(this, LoginActivity.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,binding.imageCar,binding.imageCar.getTransitionName());
            startActivity(intent,options.toBundle());

        }else {
            startActivity(intent);

        }




    }
}