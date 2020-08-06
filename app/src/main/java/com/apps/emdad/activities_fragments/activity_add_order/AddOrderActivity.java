package com.apps.emdad.activities_fragments.activity_add_order;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.ChangeClipBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_verification_code.VerificationCodeActivity;
import com.apps.emdad.adapters.CountriesAdapter;
import com.apps.emdad.databinding.ActivityAddOrderBinding;
import com.apps.emdad.databinding.ActivityLoginBinding;
import com.apps.emdad.databinding.DialogCountriesBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CountryModel;
import com.apps.emdad.models.LoginModel;
import com.apps.emdad.share.Common;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class AddOrderActivity extends AppCompatActivity {
    private ActivityAddOrderBinding binding;


    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("Lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new  TransitionSet());
            getWindow().setExitTransition(new TransitionSet());

        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_order);
        initView();
    }

    private void initView() {
        greeting();



    }

    private void greeting() {
        showDots();
        stopTyping();
    }

    private void stopTyping() {
        new Handler()
                .postDelayed(() -> {
                    binding.cardTyping.setVisibility(View.GONE);
                    binding.tvTyping.stop();
                    binding.llGreeting.setVisibility(View.VISIBLE);

                    new Handler().postDelayed(() -> {
                        binding.llHelp.setVisibility(View.VISIBLE);

                    },500);
                },2000);
    }

    private void showDots() {

        new Handler()
                .postDelayed(() -> {
                    binding.cardTyping.setVisibility(View.VISIBLE);
                    binding.tvTyping.start();
                },1000);
    }


}
