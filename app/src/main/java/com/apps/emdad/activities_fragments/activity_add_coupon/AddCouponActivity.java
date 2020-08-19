package com.apps.emdad.activities_fragments.activity_add_coupon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;

import com.apps.emdad.R;
import com.apps.emdad.databinding.ActivityAddCouponBinding;
import com.apps.emdad.databinding.ActivityAddOrderBinding;
import com.apps.emdad.language.Language;

import io.paperdb.Paper;

public class AddCouponActivity extends AppCompatActivity {
    private ActivityAddCouponBinding binding;
    private boolean canSelect = false;
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

        binding.tvSocial.setText(Html.fromHtml(getString(R.string.want_to_get_the_latest_coupons)+getEmoji(0x1F609)+" "+getString(R.string.follow_us_on)+" "+"<a href=\"https://twitter.com/\">"+getString(R.string.twitter)+"</a>"+"  ,  "+"<a href=\"https://www.instagram.com/\">"+getString(R.string.instagram)+"</a>"+"  ,  "+getString(R.string.and1)+"  "+"<a href=\"https://www.facebook.com/\">"+getString(R.string.facebook)+"</a>"));
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
}