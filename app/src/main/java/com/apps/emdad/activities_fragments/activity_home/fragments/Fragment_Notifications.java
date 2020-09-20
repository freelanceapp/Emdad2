package com.apps.emdad.activities_fragments.activity_home.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.databinding.FragmentMainBinding;
import com.apps.emdad.databinding.FragmentNotificationBinding;
import com.apps.emdad.models.UnReadCountModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.tags.Tags;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Fragment_Notifications extends Fragment {
    private HomeActivity activity;
    private FragmentNotificationBinding binding;
    private UserModel userModel;
    private Preferences preferences;

    public static Fragment_Notifications newInstance(){
        return new Fragment_Notifications();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification,container,false);
        initView();
        return binding.getRoot();

    }

    private void initView() {
        activity = (HomeActivity) getActivity();
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(activity);
        getNotifications();
    }

    private void getNotifications() {
        updateNotificationCount();
    }

    private void updateNotificationCount()
    {
        Api.getService(Tags.base_url).readNotification(userModel.getUser().getToken(),userModel.getUser().getId())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.isSuccessful()) {
                            activity.updateNotificationCount(0);
                        } else {

                            try {
                                Log.e("error_code", response.code() + response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        try {

                            if (t.getMessage() != null) {
                                Log.e("error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(activity, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else if (t.getMessage().toLowerCase().contains("socket") || t.getMessage().toLowerCase().contains("canceled")) {
                                } else {
                                    Toast.makeText(activity, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }


                        } catch (Exception e) {

                        }
                    }
                });
    }
}
