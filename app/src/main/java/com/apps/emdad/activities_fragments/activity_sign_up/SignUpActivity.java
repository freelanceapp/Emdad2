package com.apps.emdad.activities_fragments.activity_sign_up;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.apps.emdad.R;
import com.apps.emdad.activities_fragments.activity_home.HomeActivity;
import com.apps.emdad.activities_fragments.activity_splash_loading.SplashLoadingActivity;
import com.apps.emdad.databinding.ActivitySignUpBinding;
import com.apps.emdad.databinding.DialogYearBinding;
import com.apps.emdad.interfaces.Listeners;
import com.apps.emdad.language.Language;
import com.apps.emdad.models.SignUpModel;
import com.apps.emdad.models.UserModel;
import com.apps.emdad.preferences.Preferences;
import com.apps.emdad.remote.Api;
import com.apps.emdad.share.Common;
import com.apps.emdad.tags.Tags;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.paperdb.Paper;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity implements Listeners.SignUpListener {
    private ActivitySignUpBinding binding;
    private final String READ_PERM = Manifest.permission.READ_EXTERNAL_STORAGE;
    private final String write_permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private final String camera_permission = Manifest.permission.CAMERA;
    private final int READ_REQ = 1, CAMERA_REQ = 2;
    private Uri uri = null;
    private SignUpModel signUpModel;
    private Preferences preferences;
    private boolean fromSplash =true;
    private UserModel userModel;

    @Override
    protected void attachBaseContext(Context newBase) {
        Paper.init(newBase);
        super.attachBaseContext(Language.updateResources(newBase, Paper.book().read("lang", "ar")));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        initView();
        getDataFromIntent();

    }

    private void initView() {
        preferences = Preferences.getInstance();
        userModel = preferences.getUserData(this);

        signUpModel = new SignUpModel();
        binding.setModel(signUpModel);
        binding.setListener(this);
        binding.tvYearOfBirth.setOnClickListener(v -> {
            createDialogAlert();
        });


        if (userModel!=null){
            signUpModel.setCountry_id(userModel.getUser().getCountry().getId_country());
            signUpModel.setYear(userModel.getUser().getDate_of_birth());
            signUpModel.setGender(userModel.getUser().getGender());
            signUpModel.setEmail(userModel.getUser().getEmail());
            signUpModel.setName(userModel.getUser().getName());
            signUpModel.setPhone_code(userModel.getUser().getPhone_code());
            signUpModel.setPhone(userModel.getUser().getPhone());
            binding.setModel(signUpModel);
            if (userModel.getUser().getGender().equals("male")){
                male();
            }else {
                female();
            }
            binding.tvYearOfBirth.setText(signUpModel.getYear());
            Picasso.get().load(Uri.parse(Tags.IMAGE_URL+userModel.getUser().getLogo())).placeholder(R.drawable.user_avatar).into(binding.image);

            binding.btnSignUp.setText(R.string.update);

        }

    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String phone_code = intent.getStringExtra("phone_code");
            String phone = intent.getStringExtra("phone");
            String country_id = intent.getStringExtra("country_id");
            Log.e("ccccccid",country_id+"__");
            fromSplash = intent.getBooleanExtra("from",true);

            signUpModel.setPhone_code(phone_code);
            signUpModel.setPhone(phone);
            signUpModel.setCountry_id(country_id);


        }
    }

    @Override
    public void openSheet() {
        binding.expandLayout.setExpanded(true, true);
    }

    @Override
    public void closeSheet() {
        binding.expandLayout.collapse(true);

    }


    @Override
    public void checkDataValid() {

        if (signUpModel.isDataValid(this)) {
            Common.CloseKeyBoard(this, binding.edtName);

            if (userModel==null){
                signUp();

            }else {
                if (uri==null){
                    updateProfileWithoutImage();

                }else {
                    updateProfileWithImage();
                }
            }
        }

    }



    @Override
    public void checkReadPermission() {
        closeSheet();
        if (ActivityCompat.checkSelfPermission(this, READ_PERM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_PERM}, READ_REQ);
        } else {
            SelectImage(READ_REQ);
        }
    }

    @Override
    public void checkCameraPermission() {

        closeSheet();

        if (ContextCompat.checkSelfPermission(this, write_permission) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, camera_permission) == PackageManager.PERMISSION_GRANTED
        ) {
            SelectImage(CAMERA_REQ);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{camera_permission, write_permission}, CAMERA_REQ);
        }
    }

    @Override
    public void male() {
        binding.iconMale.setColorFilter(ContextCompat.getColor(this, R.color.color_blue));
        binding.iconFemale.setColorFilter(ContextCompat.getColor(this, R.color.color4));
        binding.tvMale.setTextColor(ContextCompat.getColor(this, R.color.color_blue));
        binding.tvFemale.setTextColor(ContextCompat.getColor(this, R.color.color4));
        signUpModel.setGender("male");

    }

    @Override
    public void female() {
        binding.iconMale.setColorFilter(ContextCompat.getColor(this, R.color.color4));
        binding.iconFemale.setColorFilter(ContextCompat.getColor(this, R.color.color_red));
        binding.tvMale.setTextColor(ContextCompat.getColor(this, R.color.color4));
        binding.tvFemale.setTextColor(ContextCompat.getColor(this, R.color.color_red));
        signUpModel.setGender("female");


    }

    private void SelectImage(int req) {

        Intent intent = new Intent();

        if (req == READ_REQ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            } else {
                intent.setAction(Intent.ACTION_GET_CONTENT);

            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/*");
            startActivityForResult(intent, req);

        } else if (req == CAMERA_REQ) {
            try {
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, req);
            } catch (SecurityException e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, R.string.perm_image_denied, Toast.LENGTH_SHORT).show();

            }


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_REQ) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == CAMERA_REQ) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                SelectImage(requestCode);
            } else {
                Toast.makeText(this, getString(R.string.perm_image_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQ && resultCode == Activity.RESULT_OK && data != null) {

            uri = data.getData();
            File file = new File(Common.getImagePath(this, uri));
            Picasso.get().load(file).fit().into(binding.image);


        } else if (requestCode == CAMERA_REQ && resultCode == Activity.RESULT_OK && data != null) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            uri = getUriFromBitmap(bitmap);
            if (uri != null) {
                String path = Common.getImagePath(this, uri);

                if (path != null) {
                    Picasso.get().load(new File(path)).fit().into(binding.image);

                } else {
                    Picasso.get().load(uri).fit().into(binding.image);

                }
            }


        }

    }

    private Uri getUriFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return Uri.parse(MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "", ""));
    }

    private void createDialogAlert() {
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .create();

        DialogYearBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_year, null, false);
        List<String> years = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int start = year - 50;
        int end = year - 18;
        for (int y = start; y <= end; y++) {
            years.add(String.valueOf(y));
        }


        String[] values = new String[years.size()];

        binding.picker.setMinValue(0);
        binding.picker.setMaxValue(years.size() - 1);
        binding.picker.setWrapSelectorWheel(true);
        binding.picker.setDisplayedValues(years.toArray(values));
        binding.picker.setValue(1);
        binding.imageUp.setOnClickListener(v -> {
            binding.picker.setValue(binding.picker.getValue() - 1);
        });

        binding.imageDown.setOnClickListener(v -> {
            binding.picker.setValue(binding.picker.getValue() + 1);
        });


        binding.btnOk.setOnClickListener(v ->
                {
                    dialog.dismiss();
                    signUpModel.setYear(values[binding.picker.getValue()]);
                    SignUpActivity.this.binding.tvYearOfBirth.setText(signUpModel.getYear());
                }
        );

        binding.btnCancel.setOnClickListener(v -> dialog.dismiss()

        );
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_congratulation_animation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.setView(binding.getRoot());
        dialog.show();
    }

    private void signUp() {
        if (uri == null) {
            signUpWithoutImage();
        } else {
            signUpWithImage();
        }
    }

    private void signUpWithoutImage() {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .signUpWithoutImage(signUpModel.getName(),signUpModel.getEmail(),signUpModel.getPhone_code(),signUpModel.getPhone(),signUpModel.getGender(),signUpModel.getYear(),signUpModel.getCountry_id(),"android")
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            preferences.create_update_userdata(SignUpActivity.this,response.body());
                            if (fromSplash){

                                Intent intent = new Intent(SignUpActivity.this, SplashLoadingActivity.class);
                                startActivity(intent);

                                //navigateToHomeActivity();

                            }
                            finish();
                        }else
                        {
                            try {
                                Log.e("code",response.code()+"__"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code()==500)
                            {
                                Toast.makeText(SignUpActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if (response.code()==409)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.phone_exist, Toast.LENGTH_SHORT).show();
                            }else if (response.code()==406)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.email_exist, Toast.LENGTH_SHORT).show();


                            }
                            else
                            {
                                Toast.makeText(SignUpActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });
    }

    private void signUpWithImage() {

        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Log.e("coun_id",signUpModel.getCountry_id()+"___");
        RequestBody name_part = Common.getRequestBodyText(signUpModel.getName());
        RequestBody phone_code_part = Common.getRequestBodyText(signUpModel.getPhone_code());
        RequestBody phone_part = Common.getRequestBodyText(signUpModel.getPhone());
        RequestBody email_part = Common.getRequestBodyText(signUpModel.getEmail());
        RequestBody gender_part = Common.getRequestBodyText(signUpModel.getGender());
        RequestBody year_part = Common.getRequestBodyText(signUpModel.getYear());
        RequestBody country_id_part = Common.getRequestBodyText(signUpModel.getCountry_id());
        RequestBody software_part = Common.getRequestBodyText("android");

        MultipartBody.Part image = Common.getMultiPart(this,uri,"logo");


        Api.getService(Tags.base_url)
                .signUpWithImage(name_part,email_part,phone_code_part,phone_part,gender_part,year_part,country_id_part,software_part,image)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            preferences.create_update_userdata(SignUpActivity.this,response.body());

                            if (fromSplash){

                                Intent intent = new Intent(SignUpActivity.this, SplashLoadingActivity.class);
                                startActivity(intent);

                                //navigateToHomeActivity();

                            }
                            finish();

                        }else
                        {

                            try {
                                Log.e("code",response.code()+"__"+response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (response.code()==500)
                            {
                                Toast.makeText(SignUpActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if (response.code()==409)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.phone_exist, Toast.LENGTH_SHORT).show();
                            }else if (response.code()==406)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.email_exist, Toast.LENGTH_SHORT).show();


                            }else
                            {
                                Toast.makeText(SignUpActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });

    }


    private void updateProfileWithoutImage()
    {

        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        Api.getService(Tags.base_url)
                .updateProfileWithoutImage(userModel.getUser().getToken(),userModel.getUser().getId(),signUpModel.getName(),signUpModel.getEmail(),signUpModel.getPhone(),signUpModel.getPhone_code(),signUpModel.getGender(),signUpModel.getYear())
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            preferences.create_update_userdata(SignUpActivity.this,response.body());
                            setResult(RESULT_OK);
                            finish();
                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(SignUpActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if (response.code()==409)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.phone_exist, Toast.LENGTH_SHORT).show();
                            }else if (response.code()==406)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.email_exist, Toast.LENGTH_SHORT).show();


                            }
                            else
                            {
                                Toast.makeText(SignUpActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }

                            try {
                                Log.e("error",response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });

    }
    private void updateProfileWithImage() {
        ProgressDialog dialog = Common.createProgressDialog(this,getString(R.string.wait));
        dialog.setCancelable(false);
        dialog.show();
        RequestBody user_id = Common.getRequestBodyText(String.valueOf(userModel.getUser().getId()));
        RequestBody name_part = Common.getRequestBodyText(signUpModel.getName());
        RequestBody phone_code_part = Common.getRequestBodyText(signUpModel.getPhone_code());
        RequestBody phone_part = Common.getRequestBodyText(signUpModel.getPhone());
        RequestBody email_part = Common.getRequestBodyText(signUpModel.getEmail());
        RequestBody gender_part = Common.getRequestBodyText(signUpModel.getGender());
        RequestBody year_part = Common.getRequestBodyText(signUpModel.getYear());

        MultipartBody.Part image = Common.getMultiPart(this,uri,"logo");


        Api.getService(Tags.base_url)
                .updateProfileWithImage(userModel.getUser().getToken(),user_id,name_part,email_part,phone_part,phone_code_part,gender_part,year_part,image)
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        dialog.dismiss();
                        if (response.isSuccessful()&&response.body()!=null)
                        {
                            preferences.create_update_userdata(SignUpActivity.this,response.body());
                            setResult(RESULT_OK);
                            finish();

                        }else
                        {
                            if (response.code()==500)
                            {
                                Toast.makeText(SignUpActivity.this, "Server Error", Toast.LENGTH_SHORT).show();
                            }else if (response.code()==409)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.phone_exist, Toast.LENGTH_SHORT).show();
                            }else if (response.code()==406)
                            {
                                Toast.makeText(SignUpActivity.this, R.string.email_exist, Toast.LENGTH_SHORT).show();


                            }else
                            {
                                Toast.makeText(SignUpActivity.this,getString(R.string.failed), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        try {
                            dialog.dismiss();
                            if (t.getMessage() != null) {
                                Log.e("msg_category_error", t.getMessage() + "__");

                                if (t.getMessage().toLowerCase().contains("failed to connect") || t.getMessage().toLowerCase().contains("unable to resolve host")) {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.something), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignUpActivity.this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }catch (Exception e)
                        {
                            Log.e("Error",e.getMessage()+"__");
                        }
                    }
                });

    }

    private void navigateToHomeActivity()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
