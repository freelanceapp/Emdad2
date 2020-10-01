package com.apps.emdad.activities_fragments.activity_add_coupon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.databinding.ActivityAddCouponBinding;
import com.apps.emdad.databinding.ActivityAddOrderBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.SettingModel;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;

import java.io.IOException;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCouponActivity extends AppCompatActivity {
    private ActivityAddCouponBinding binding;
    private boolean canSelect = false;
    private SettingModel settingModel;
    private String lang;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_coupon);
        initView();
    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang","ar");

        binding.tvSocial.setMovementMethod(LinkMovementMethod.getInstance());

        binding.close.setOnClickListener(v -> finish());
        binding.edtCoupon.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()){
                    canSelect = false;
                }else {
                    canSelect = true;
                }
                updateBtnUi();
            }
        });

        binding.btnVerify.setOnClickListener(v -> verifyCoupon());

        getSetting();

    }

    private void verifyCoupon() {
        String coupon = binding.edtCoupon.getText().toString().trim();

        Intent intent = getIntent();
        setResult(RESULT_OK,intent);
        finish();

    }

    private void updateBtnUi() {
        if (canSelect){
            binding.btnVerify.setBackgroundResource(R.drawable.small_rounded_primary);
            binding.btnVerify.setTextColor(ContextCompat.getColor(this,R.color.white));
        }else {
            binding.btnVerify.setBackgroundResource(R.drawable.small_rounded_gray);
            binding.btnVerify.setTextColor(ContextCompat.getColor(this,R.color.gray9));
        }
    }


    private String getEmoji(int unicode){

        return new String(Character.toChars(unicode));
    }

    private void getSetting(){
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Api.getService(Tags.base_url).getSetting(lang)
                .enqueue(new Callback<SettingModel>() {
                    @Override
                    public void onResponse(Call<SettingModel> call, Response<SettingModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                settingModel = response.body();

                                binding.tvSocial.setText(Html.fromHtml(getString(R.string.want_to_get_the_latest_coupons)+getEmoji(0x1F609)+" "+getString(R.string.follow_us_on)+" "+"<a href="+settingModel.getSettings().getTwitter()+">"+getString(R.string.twitter)+"</a>"+"  ,  "+"<a href="+settingModel.getSettings().getInstagram()+">"+getString(R.string.instagram)+"</a>"+"  ,  "+getString(R.string.and1)+"  "+"<a href="+settingModel.getSettings().getFacebook()+">"+getString(R.string.facebook)+"</a>"));

                            }
                        } else {
                            binding.tvSocial.setText(Html.fromHtml(getString(R.string.want_to_get_the_latest_coupons)+getEmoji(0x1F609)+" "+getString(R.string.follow_us_on)+" "+"<a href=\"https://twitter.com/\">"+getString(R.string.twitter)+"</a>"+"  ,  "+"<a href=\"https://www.instagram.com/\">"+getString(R.string.instagram)+"</a>"+"  ,  "+getString(R.string.and1)+"  "+"<a href=\"https://www.facebook.com/\">"+getString(R.string.facebook)+"</a>"));

                            dialog.dismiss();

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<SettingModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            binding.tvSocial.setText(Html.fromHtml(getString(R.string.want_to_get_the_latest_coupons)+getEmoji(0x1F609)+" "+getString(R.string.follow_us_on)+" "+"<a href=\"https://twitter.com/\">"+getString(R.string.twitter)+"</a>"+"  ,  "+"<a href=\"https://www.instagram.com/\">"+getString(R.string.instagram)+"</a>"+"  ,  "+getString(R.string.and1)+"  "+"<a href=\"https://www.facebook.com/\">"+getString(R.string.facebook)+"</a>"));

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(AddCouponActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(AddCouponActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }
}