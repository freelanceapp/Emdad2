package com.apps.emdad.activities_fragments.activity_old_orders;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.os.Bundle;

import com.apps.emdad.R;
import com.apps.emdad.databinding.ActivityAddCouponBinding;
import com.apps.emdad.databinding.ActivityOldOrdersBinding;
import com.apps.emdad.language.Language;

import io.paperdb.Paper;

public class OldOrdersActivity extends AppCompatActivity {
    private ActivityOldOrdersBinding binding;
    private String lang;
    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_old_orders);
        initView();
    }

    private void initView() {
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.flBack.setOnClickListener(v -> finish());
    }
}