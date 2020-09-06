package com.apps.emdad.activities_fragments.activity_login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.AutoTransition;
import android.transition.ChangeTransform;
import android.transition.Explode;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_shop_details.ShopDetailsActivity;
import com.apps.emdad.activities_fragments.activity_shops.ShopsActivity;
import com.apps.emdad.activities_fragments.activity_verification_code.VerificationCodeActivity;
import com.apps.emdad.adapters.CountriesAdapter;
import com.apps.emdad.databinding.ActivityLoginBinding;
import com.apps.emdad.databinding.DialogCountriesBinding;
import com.apps.emdad.interfaces.Listeners;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.CountryDataModel;
import com.apps.emdad.models.CountryModel;
import com.apps.emdad.models.LoginModel;
import com.apps.emdad.models.PlaceDetailsModel;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Listeners.LoginListener {
    private ActivityLoginBinding binding;
    private LoginModel loginModel;
    private List<CountryModel> countryModelList;
    private CountriesAdapter countriesAdapter;
    private AlertDialog dialog;
    private String lang;
    private String phone_code = "+966";
    private String country_id="";
    private boolean fromSplash = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new TransitionSet());
            getWindow().setExitTransition(new TransitionSet());


        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        getDataFromIntent();
        initView();
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        fromSplash = intent.getBooleanExtra("from",true);

    }

    private void initView() {
        countryModelList = new ArrayList<>();

        loginModel = new LoginModel();
        binding.setLoginModel(loginModel);
        binding.setListener(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.lanuch);
        binding.cons.startAnimation(animation);
        binding.edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().startsWith("0"))
                {
                    binding.edtPhone.setText("");
                }
            }
        });
        binding.tvSkip.setOnClickListener(v -> {
            navigateToHomeActivity();
        });
        createCountriesDialog();
        getPhoneCodes();
        if (!fromSplash){
            binding.tvSkip.setVisibility(View.GONE);
        }
    }



    private void getPhoneCodes() {
        Api.getService(Tags.base_url)
                .getCountries(lang)
                .enqueue(new Callback<CountryDataModel>() {
                    @Override
                    public void onResponse(Call<CountryDataModel> call, Response<CountryDataModel> response) {
                        binding.progBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            countryModelList.clear();
                            countryModelList.addAll(response.body().getData());
                            countriesAdapter.notifyDataSetChanged();

                            if (countryModelList.size()>0){
                                binding.arrow.setVisibility(View.VISIBLE);
                            }else {
                                binding.arrow.setVisibility(View.GONE);

                            }

                        } else {
                            binding.progBar.setVisibility(View.GONE);

                            try {
                                Log.e("error_code",response.code()+"_"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<CountryDataModel> call, Throwable t) {
                        try {
                            binding.progBar.setVisibility(View.GONE);

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage());
                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(LoginActivity.this, getString(R.string.something), Toast.LENGTH_LONG).show();
                                }
                                else if (t.getMessage().toLowerCase().contains("socket")||t.getMessage().toLowerCase().contains("canceled")){ }
                                else {
                                    Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (Exception e) {

                        }
                    }
                });

    }


    private void createCountriesDialog() {

        dialog = new AlertDialog.Builder(this)
                .create();
        countriesAdapter = new CountriesAdapter(countryModelList,this);

        DialogCountriesBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_countries, null, false);
        binding.recView.setLayoutManager(new LinearLayoutManager(this));
        binding.recView.setAdapter(countriesAdapter);

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());

    }



    @Override
    public void validate() {
        if (loginModel.isDataValid(this))
        {
            Common.CloseKeyBoard(this,binding.edtPhone);
            navigateToVerificationCodeActivity();
        }
    }

    @Override
    public void showCountryDialog() {
        if (countryModelList.size()>0){
            dialog.show();

        }
    }

    private void navigateToVerificationCodeActivity() {

        Intent intent = new Intent(this, VerificationCodeActivity.class);
        intent.putExtra("phone_code",phone_code);
        intent.putExtra("phone",loginModel.getPhone());
        intent.putExtra("country_id",country_id);
        intent.putExtra("from",fromSplash);

        startActivity(intent);
        finish();

    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void setItemData(CountryModel countryModel) {
        dialog.dismiss();
        phone_code = "+"+countryModel.getPhone_code();
        binding.tvCode.setText(phone_code);
        country_id = countryModel.getId_country();
    }
}
