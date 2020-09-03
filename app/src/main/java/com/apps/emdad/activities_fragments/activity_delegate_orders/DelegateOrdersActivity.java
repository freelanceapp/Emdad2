package com.apps.emdad.activities_fragments.activity_delegate_orders;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_setting.SettingsActivity;
import com.apps.emdad.databinding.ActivityDelegateOrdersBinding;
import com.apps.emdad.databinding.ActivityOldOrdersBinding;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;

import java.io.IOException;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DelegateOrdersActivity extends AppCompatActivity {
    private ActivityDelegateOrdersBinding binding;
    private String lang;
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_delegate_orders);
        initView();
    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);
        Paper.init(this);
        lang = Paper.book().read("lang","ar");
        binding.setLang(lang);
        binding.llback.setOnClickListener(v -> finish());
        binding.setModel(userModel);
        binding.btnAlert.setOnClickListener(v -> {
            if (userModel!=null){
                if (userModel.getUser().getReceive_notifications().equals("yes")){
                    updateReceiveNotification("no");
                }
            }
        });

        binding.btnEnableAlerts.setOnClickListener(v -> {

            if (userModel!=null){
                if (userModel.getUser().getReceive_notifications().equals("no")){
                    updateReceiveNotification("yes");
                }
            }
        });
    }


    private void updateReceiveNotification(String state) {
        Api.getService(Tags.base_url)
                .updateReceiveNotification(userModel.getUser().getToken(),userModel.getUser().getId(),state)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        if (response.isSuccessful()) {

                            userModel = response.body();
                            binding.setModel(userModel);
                            preferences.create_update_userdata(DelegateOrdersActivity.this,userModel);
                        } else {
                            try {
                                Log.e("error", response.code() + "__" + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            if (response.code() == 500) {
                                Toast.makeText(DelegateOrdersActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(DelegateOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(DelegateOrdersActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(DelegateOrdersActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("Error", e.getMessage() + "__");
                        }
                    }
                });
    }

}